package com.basilios.basilios.app.dto.product;

import jakarta.validation.constraints.NotNull;

public class ProductStatusDTO {
    @NotNull
    private Boolean isPaused;

    public Boolean getIsPaused() {
        return isPaused;
    }

    public void setIsPaused(Boolean isPaused) {
        this.isPaused = isPaused;
    }
}
