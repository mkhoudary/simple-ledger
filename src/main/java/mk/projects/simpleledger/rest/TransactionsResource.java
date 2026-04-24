/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mk.projects.simpleledger.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import mk.projects.simpleledger.core.DatabaseManager;
import mk.projects.simpleledger.utils.GsonUtils;
import mk.projects.simpleledger.utils.LedgerUtils;
import mk.projects.simpleledger.utils.ResponseUtils;
import mk.projects.simpleledger.utils.Utils;

/**
 * REST Web Service
 *
 * @author mkhoudary
 */
@Path("/Transactions")
public class TransactionsResource {

    @POST
    @Consumes(ResponseUtils.JSON_UTF8)
    @Produces(ResponseUtils.JSON_UTF8)
    public Response createTransaction(String body) throws SQLException {
        try ( Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);

            try {
                JsonObject json = GsonUtils.INSTANCE.fromJson(body, JsonObject.class);
                
                String externalId = GsonUtils.getNotBlankString(json, "externalId", "'externalId' field is mandatory");
                String idempotencyId = GsonUtils.getNotBlankString(json, "idempotencyId", "'idempotencyId' field is mandatory");
                String transactionType = GsonUtils.getNotBlankString(json, "type", "'type' field is mandatory");
                JsonArray journalEntries = GsonUtils.getJsonArray(json, "journalEntries", null);

                if (journalEntries == null || journalEntries.size() < 2) {
                    throw new IllegalArgumentException("'journalEntries' field is mandatory and must contain at least two entries");
                }

                long existingTransactionId = findExistingTransactionIdByIdempotency(con, idempotencyId);
                
                if (existingTransactionId > 0) {
                    con.rollback();

                    String existingResponse = GsonUtils.jsonObjectBuilder()
                            .prop("id", existingTransactionId)
                            .build()
                            .toString();

                    return Response.ok(existingResponse)
                            .type(ResponseUtils.JSON_UTF8)
                            .build();
                }

                LedgerUtils.instance().validateTransactionTypeCode(con, transactionType);

                List<JournalEntryData> entries = parseAndValidateJournalEntries(journalEntries);
                
                validateBalancedEntries(entries);

                long transactionId = insertTransaction(con, idempotencyId, transactionType, externalId);
                
                insertJournalEntries(con, transactionId, entries);

                con.commit();

                String response = GsonUtils.jsonObjectBuilder()
                        .prop("id", transactionId)
                        .build()
                        .toString();

                return Response.ok(response)
                        .type(ResponseUtils.JSON_UTF8)
                        .build();
            } catch (Exception ex) {
                con.rollback();

                throw ex;
            }
        }
    }

    private long findExistingTransactionIdByIdempotency(Connection con, String idempotencyId) throws SQLException {
        String sql = "SELECT id FROM sld_transactions WHERE idempotency_key = ?";

        try ( PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Utils.instance().safeString(idempotencyId));

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        return -1;
    }

    private List<JournalEntryData> parseAndValidateJournalEntries(JsonArray journalEntries) {
        List<JournalEntryData> entries = new ArrayList<>();

        for (int i = 0; i < journalEntries.size(); i++) {
            JsonElement element = journalEntries.get(i);
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException(String.format("'journalEntries[%d]' must be an object", i));
            }

            JsonObject entryJson = element.getAsJsonObject();
            long accountId = getMandatoryPositiveLong(entryJson, "accountId", i);
            String entryType = GsonUtils.getNotBlankString(entryJson, "type", String.format("'journalEntries[%d].type' field is mandatory", i));

            if (!Utils.instance().in(entryType, "DEBIT", "CREDIT")) {
                throw new IllegalArgumentException(String.format("'journalEntries[%d].type' must be either 'DEBIT' or 'CREDIT'", i));
            }

            BigDecimal amount = getMandatoryPositiveAmount(entryJson, i);
            entries.add(new JournalEntryData(accountId, entryType, amount));
        }

        return entries;
    }

    private long getMandatoryPositiveLong(JsonObject source, String key, int index) {
        if (!source.has(key) || source.get(key).isJsonNull()) {
            throw new IllegalArgumentException(String.format("'journalEntries[%d].%s' field is mandatory", index, key));
        }

        try {
            long value = source.get(key).getAsLong();
            if (value <= 0) {
                throw new IllegalArgumentException(String.format("'journalEntries[%d].%s' must be a positive number", index, key));
            }

            return value;
        } catch (NumberFormatException | UnsupportedOperationException ex) {
            throw new IllegalArgumentException(String.format("'journalEntries[%d].%s' must be a valid number", index, key));
        }
    }

    private BigDecimal getMandatoryPositiveAmount(JsonObject source, int index) {
        if (!source.has("amount") || source.get("amount").isJsonNull()) {
            throw new IllegalArgumentException(String.format("'journalEntries[%d].amount' field is mandatory", index));
        }

        BigDecimal amount;
        try {
            amount = source.get("amount").getAsBigDecimal();
        } catch (NumberFormatException | UnsupportedOperationException ex) {
            throw new IllegalArgumentException(String.format("'journalEntries[%d].amount' must be a valid number", index));
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(String.format("'journalEntries[%d].amount' must be greater than zero", index));
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateBalancedEntries(List<JournalEntryData> entries) {
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;

        for (JournalEntryData entry : entries) {
            if ("DEBIT".equals(entry.type)) {
                debitTotal = debitTotal.add(entry.amount);
            } else if ("CREDIT".equals(entry.type)) {
                creditTotal = creditTotal.add(entry.amount);
            }
        }

        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new IllegalArgumentException("Total debit amount must equal total credit amount");
        }

        if (debitTotal.equals(BigDecimal.ZERO) || creditTotal.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Transaction value must be more bigger than 0");
        }
    }

    private long insertTransaction(Connection con, String idempotencyId, String transactionType, String externalId) throws SQLException {
        String sql = "INSERT INTO sld_transactions (idempotency_key, transaction_type_code, external_id) VALUES (?, ?, ?)";

        try ( PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, Utils.instance().safeString(idempotencyId));
            ps.setString(2, Utils.instance().safeString(transactionType));
            ps.setString(3, Utils.instance().safeString(externalId));

            ps.executeUpdate();

            try ( ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Failed to create transaction");
                }

                return rs.getLong(1);
            }
        }
    }

    private void insertJournalEntries(Connection con, long transactionId, List<JournalEntryData> entries) throws SQLException {
        String sql = "INSERT INTO sld_journal_entries (transaction_id, account_id, type, amount) VALUES (?, ?, ?, ?)";

        try ( PreparedStatement ps = con.prepareStatement(sql)) {
            for (JournalEntryData entry : entries) {
                ps.setLong(1, transactionId);
                ps.setLong(2, entry.accountId);
                ps.setString(3, entry.type);
                ps.setBigDecimal(4, entry.amount);
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    private static class JournalEntryData {

        private final long accountId;
        private final String type;
        private final BigDecimal amount;

        public JournalEntryData(long accountId, String type, BigDecimal amount) {
            this.accountId = accountId;
            this.type = type;
            this.amount = amount;
        }
    }
}
