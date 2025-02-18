package com.gmail.deniska1406sme.test_task.Parsers;

public class TradeParserFactory {

    public static TradeParser getTradeParser(String format) {
        return switch (format.toLowerCase()) {
            case "csv" -> new CsvTradeParser();
            case "json" -> new JsonTradeParser();
            case "xml" -> new XmlTradeParser();
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }
}
