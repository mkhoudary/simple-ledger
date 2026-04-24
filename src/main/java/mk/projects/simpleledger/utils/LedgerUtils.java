/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mk.projects.simpleledger.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Mohammad
 */
public class LedgerUtils {

    private static LedgerUtils instance;
    
    private final Set<String> transactionTypeCodes = new HashSet<>();
    private volatile boolean transactionTypeCodesLoaded = false;

    private LedgerUtils() {
    }

    public static LedgerUtils instance() {
        if (instance == null) {
            instance = new LedgerUtils();
        }

        return instance;
    }

    public void validateTransactionTypeCode(Connection con, String code) throws SQLException {
        if (StringUtils.isBlank(code)) {
            throw new IllegalArgumentException("'type' field is mandatory");
        }

        loadTransactionTypeCodesIfNeeded(con);

        if (!transactionTypeCodes.contains(code.trim())) {
            throw new IllegalArgumentException(String.format("Invalid transaction type code '%s'", code));
        }
    }

    private void loadTransactionTypeCodesIfNeeded(Connection con) throws SQLException {
        if (transactionTypeCodesLoaded) {
            return;
        }

        synchronized (this) {
            if (transactionTypeCodesLoaded) {
                return;
            }

            String sql = "SELECT code FROM sld_transaction_types";
            
            try ( PreparedStatement ps = con.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactionTypeCodes.add(rs.getString("code"));
                }
            }

            transactionTypeCodesLoaded = true;
        }
    }
}
