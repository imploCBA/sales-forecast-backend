package com.example.salesforecast;

import lombok.Data;

@Data
public class ForecastRequest {
    private String startDate;
    private String endDate;
    private int quarters;
}
