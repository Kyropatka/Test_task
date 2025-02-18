package com.gmail.deniska1406sme.test_task.Parsers;

import com.gmail.deniska1406sme.test_task.Model.Trade;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface TradeParser {

    List<Trade> parseTrades(InputStream inputStream) throws IOException;

    Flux<Trade> parseTradesFlux(InputStream inputStream) throws IOException;

}
