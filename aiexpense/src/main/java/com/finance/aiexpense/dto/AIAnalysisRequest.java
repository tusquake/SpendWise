package com.finance.aiexpense.dto;

import lombok.Data;
import java.util.List;

@Data
public class AIAnalysisRequest {
    private List<TransactionDTO> transactions;
}