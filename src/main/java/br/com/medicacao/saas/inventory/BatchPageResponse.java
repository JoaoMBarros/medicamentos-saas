package br.com.medicacao.saas.inventory;

import java.util.List;

import org.springframework.data.domain.Page;

public record BatchPageResponse(
        List<BatchResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    static BatchPageResponse from(Page<StockBatch> batches) {
        return new BatchPageResponse(batches.map(BatchResponse::from).getContent(),
                batches.getNumber(), batches.getSize(), batches.getTotalElements(),
                batches.getTotalPages());
    }
}
