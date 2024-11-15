package org.example.service;

import org.example.entity.response.Response;
import org.example.entity.response.ResponseItem;

import java.util.List;

public interface ScoringService {

    void calcScore(Response response);

    void reCalcScore(Response response, List<ResponseItem> items);
}
