package com.gmail.deniska1406sme.test_task;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface TradeParser {

    List<Trade> parseTrades(InputStream inputStream) throws IOException;

}
