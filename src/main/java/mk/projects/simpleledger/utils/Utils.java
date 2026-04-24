/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mk.projects.simpleledger.utils;

import com.google.gson.JsonPrimitive;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Whitelist;

/**
 *
 * @author Mohammed
 */
public class Utils {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private final static Document.OutputSettings PRETTY_PRINT = new Document.OutputSettings().prettyPrint(false).charset("UTF-8").escapeMode(Entities.EscapeMode.xhtml);

    private static Utils instance;

    private Utils() {

    }

    public String leftPad(String toPad, String padding, int count) {
        return StringUtils.leftPad(toPad, count, padding);
    }

    public String rightPad(String toPad, String padding, int count) {
        return StringUtils.rightPad(toPad, count, padding);
    }

    public boolean in(Object item, Object... list) {
        return Arrays.stream(list).filter(anItem -> item.equals(anItem)).count() > 0;
    }

    public String arrayToString(Object[] array, String delimiter) {
        return Arrays.stream(array).map(object -> object.toString()).collect(Collectors.joining(delimiter));
    }

    public <T> T ifNull(T toCheck, T alternative) {
        if (toCheck == null) {
            return alternative;
        }

        return toCheck;
    }

    public String ifBlank(String toCheck, String alternative) {
        if (StringUtils.isBlank(toCheck)) {
            return alternative;
        }

        return toCheck;
    }

    public boolean isNull(Object toCheck) {
        return toCheck == null;
    }

    public boolean isNumber(Object toCheck) {
        if (toCheck == null) {
            return false;
        }

        try {
            Double.parseDouble(toCheck.toString());

            return true;
        } catch (Exception x) {
            return false;
        }
    }

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public Map.Entry entry(String key, Object value) {
        return new AbstractMap.SimpleEntry(key, value);
    }

    public List asList(Object... values) {
        return Arrays.asList(values);
    }

    public List newList() {
        return new ArrayList<>();
    }

    public Set newSet() {
        return new HashSet<>();
    }

    public List emptyList() {
        return Collections.EMPTY_LIST;
    }

    public Map newMap() {
        return new HashMap();
    }

    public Map emptyMap() {
        return Collections.EMPTY_MAP;
    }

    public String formatDate(Object date, String pattern) throws ParseException {
        Date parsedDate = parseDate(date);

        return new SimpleDateFormat(pattern).format(parsedDate);
    }

    public Date parseDate(Object date) {
        return parseDate(date, DATE_FORMAT);
    }

    public Date parseDate(Object date, String pattern) {
        return parseDate(date, new SimpleDateFormat(pattern));
    }

    public Date parseDate(Object date, SimpleDateFormat format) {
        try {
            if (date == null) {
                return null;
            } else if (date instanceof String) {
                if (StringUtils.isBlank((String) date)) {
                    return null;
                }

                return format.parse((String) date);
            } else if (date instanceof Long || date.getClass().equals(Long.TYPE)) {
                return new Date((long) date);
            } else if (date instanceof Date) {
                return (Date) date;
            } else {
                if (StringUtils.isBlank(date.toString())) {
                    return null;
                }

                return format.parse(date.toString());
            }
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String urlEncode(String toEncode) throws UnsupportedEncodingException {
        return URLEncoder.encode(toEncode, "UTF-8");
    }

    public boolean isBlank(Object object) {
        if (object == null) {
            return true;
        }
        
        if (object instanceof String) {
            return StringUtils.isBlank((String) object);
        }
        
        return false;
    }

    public boolean isNotBlank(String string) {
        return StringUtils.isNotBlank(string);
    }

    public String safeString(String string) {
        if (StringUtils.isBlank(string)) {
            return string;
        }
        
        String cleanString = Jsoup.clean(string, "", Whitelist.none(), PRETTY_PRINT);

        cleanString = cleanString.trim()
                .replaceAll("'", "")
                .replaceAll("\"", "")
                .replaceAll("&[a-zA-Z][a-zA-Z0-9]+;", "");

        return cleanString;
    }

    public boolean matches(String text, String expression) {
        return Pattern.matches(expression, text);
    }

    public String format(String template, Object... parameters) {
        return String.format(template, parameters);
    }

    public Double toDouble(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Double) {
            return (Double) object;
        } else if (object instanceof String) {
            String str = ((String) object).trim();
            if (str.isEmpty()) return null;
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (object instanceof JsonPrimitive) {
            try {
                return ((JsonPrimitive) object).getAsDouble();
            } catch (NumberFormatException | UnsupportedOperationException e) {
                return null;
            }
        } else if (object instanceof BigDecimal) {
            return ((BigDecimal) object).doubleValue();
        } else if (object instanceof Number) {
            try {
                return ((Number) object).doubleValue();
            } catch (NumberFormatException | UnsupportedOperationException e) {
                return null;
            }
        } else {
            String str = object.toString().trim();
            if (str.isEmpty()) return null;
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public BigDecimal round(Number number, int places) {
        if (places < 0) {
            places = 2;
        }

        if (number == null) {
            throw new IllegalArgumentException();
        }

        BigDecimal value = new BigDecimal(number.toString());
        return value.setScale(places, RoundingMode.CEILING);
    }

    public Number toNumber(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return (Number) object;
        } else if (object instanceof Number) {
            return (Number) object;
        } else if (object instanceof String) {
            return Double.parseDouble((String) object);
        } else if (object instanceof JsonPrimitive) {
            return ((JsonPrimitive) object).getAsNumber();
        } else {
            return Double.parseDouble(object.toString());
        }
    }

    public Boolean toBoolean(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object.getClass() == Boolean.TYPE) {
            return (Boolean) object;
        } else if (object instanceof String) {
            return Boolean.parseBoolean((String) object);
        } else if (object instanceof JsonPrimitive) {
            return ((JsonPrimitive) object).getAsBoolean();
        } else {
            return Boolean.parseBoolean(object.toString());
        }
    }

    public BigDecimal ceil(BigDecimal number) {
        return BigDecimal.valueOf(Math.ceil(number.doubleValue()));
    }

    public BigDecimal floor(BigDecimal number) {
        return BigDecimal.valueOf(Math.floor(number.doubleValue()));
    }

    public BigDecimal toBigDecimal(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof BigDecimal) {
            return (BigDecimal) object;
        } else if (object instanceof String) {
            return new BigDecimal((String) object);
        } else if (object instanceof JsonPrimitive) {
            return ((JsonPrimitive) object).getAsBigDecimal();
        } else {
            return new BigDecimal(object.toString());
        }
    }

    public Integer toInteger(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Integer) {
            return (Integer) object;
        } else if (object.getClass() == Integer.TYPE) {
            return (Integer) object;
        } else if (object instanceof Number) {
            return ((Number) object).intValue();
        } else if (object instanceof String) {
            return new Double((String) object).intValue();
        } else if (object instanceof JsonPrimitive) {
            return ((JsonPrimitive) object).getAsInt();
        } else {
            return new Double(object.toString()).intValue();
        }
    }

    public Long toLong(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Long) {
            return (Long) object;
        } else if (object.getClass() == Integer.TYPE) {
            return (Long) object;
        } else if (object instanceof String) {
            return Double.valueOf((String) object).longValue();
        } else if (object instanceof JsonPrimitive) {
            return ((JsonPrimitive) object).getAsLong();
        } else {
            return Double.valueOf(object.toString()).longValue();
        }
    }

    public String toString(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof String) {
            return (String) object;
        } else {
            return object.toString();
        }
    }

    public List sortList(List toSort, ScriptObjectMirror sorter) {
        toSort.sort((o1, o2) -> {
            return new BigDecimal(sorter.call(null, o1, o2).toString()).intValue();
        });

        return toSort;
    }

    public String formatString(String pattern, Object... attributes) {
        return String.format(pattern, attributes);
    }

    public Date now() {
        return new Date();
    }

    public Date today() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public Date dateAdd(Date date, Integer quantity, String unit) {
        int selectedUnit;

        switch (unit) {
            case "DAY":
                selectedUnit = Calendar.DATE;
                break;
            case "MONTH":
                selectedUnit = Calendar.MONTH;
                break;
            case "YEAR":
                selectedUnit = Calendar.YEAR;
                break;
            case "HOUR":
                selectedUnit = Calendar.HOUR_OF_DAY;
                break;
            case "MINUTE":
                selectedUnit = Calendar.MINUTE;
                break;
            case "SECOND":
                selectedUnit = Calendar.SECOND;
                break;
            default:
                selectedUnit = 0;
                break;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(selectedUnit, quantity);

        return calendar.getTime();
    }

    public Date dateSet(Date date, Integer quantity, String unit) {
        int selectedUnit;

        switch (unit) {
            case "DAY":
                selectedUnit = Calendar.DATE;
                break;
            case "MONTH":
                selectedUnit = Calendar.MONTH;
                break;
            case "YEAR":
                selectedUnit = Calendar.YEAR;
                break;
            case "HOUR":
                selectedUnit = Calendar.HOUR_OF_DAY;
                break;
            case "MINUTE":
                selectedUnit = Calendar.MINUTE;
                break;
            case "SECOND":
                selectedUnit = Calendar.SECOND;
                break;
            default:
                selectedUnit = 0;
                break;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(selectedUnit, quantity);

        return calendar.getTime();
    }

    public static Utils instance() {
        if (instance == null) {
            instance = new Utils();
        }

        return instance;
    }

}
