package com.gmail.deniska1406sme.test_task.Parsers;

public class TradeParserFactory {

    public static TradeParser getTradeParser(String format) {
        switch (format.toLowerCase()) {
            case "csv":
                return new CsvTradeParser();
            case "json":
                return new JsonTradeParser();
            case "xml":
                return new XmlTradeParser();
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}
