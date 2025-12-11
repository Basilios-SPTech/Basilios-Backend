package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.dashboard.ChampionDTO;
import com.basilios.basilios.core.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ========================= TESTE DO DASHBOARD CONTROLLER =========================
public class DashboardControllerTest {

    private MockMvc mockMvc;
    private FakeDashboardService fakeDashboardService;

    @BeforeEach
    void setUp() throws Exception {
        fakeDashboardService = new FakeDashboardService();

        DashboardController controller = new DashboardController();

        // ==== INJETAR O SERVICE POR REFLECTION ====
        Field serviceField = DashboardController.class.getDeclaredField("dashboardService");
        serviceField.setAccessible(true);
        serviceField.set(controller, fakeDashboardService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ========= Cenário POSITIVO =========
    @Test
    void testGetChampionSuccess() throws Exception {
        fakeDashboardService.returnChampion = Optional.of(
                new ChampionDTO(20L, "Burger Supremo", 120, false)
        );

        mockMvc.perform(
                        get("/dashboard/champion")
                                .param("dta_inicio", "2024-01-01")
                                .param("dta_fim", "2024-12-31")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Burger Supremo"))
                .andExpect(jsonPath("$.unitsSold").value(120));
    }

    // ========= Cenário NEGATIVO =========
    @Test
    void testGetChampionNotFound() throws Exception {
        fakeDashboardService.returnChampion = Optional.empty();

        mockMvc.perform(
                        get("/dashboard/champion")
                                .param("dta_inicio", "2024-01-01")
                                .param("dta_fim", "2024-12-31")
                )
                .andExpect(status().isNotFound());
    }
}

class FakeDashboardService extends DashboardService {

    public Optional<ChampionDTO> returnChampion = Optional.empty();

    @Override
    public Optional<ChampionDTO> getChampionOfPeriod(LocalDate start, LocalDate end) {
        return returnChampion;
    }

    // ==== MÉTODOS NÃO USADOS NOS TESTES ====
    @Override public com.basilios.basilios.app.dto.dashboard.RevenueDTO getRevenue(LocalDate start, LocalDate end) { return null; }
    @Override public com.basilios.basilios.app.dto.dashboard.OrdersCountDTO getOrdersCount(LocalDate start, LocalDate end) { return null; }
    @Override public com.basilios.basilios.app.dto.dashboard.AverageTicketDTO getAverageTicket(LocalDate start, LocalDate end) { return null; }
    @Override public long getItemsSold(java.time.LocalDateTime start, java.time.LocalDateTime end) { return 0; }
    @Override public com.basilios.basilios.app.dto.dashboard.CancellationRateDTO getCancellationRate(LocalDate start, LocalDate end) { return null; }
    @Override public com.basilios.basilios.app.dto.dashboard.AverageDeliveryTimeDTO getAverageDeliveryTime(LocalDate start, LocalDate end) { return null; }
    @Override public com.basilios.basilios.app.dto.dashboard.OrderPeaksDTO getOrderPeaks(LocalDate start, LocalDate end) { return null; }
    @Override public java.util.List<com.basilios.basilios.app.dto.dashboard.TopProductDTO> getTopProductsByUnits(LocalDate start, LocalDate end, int limit) { return null; }
}
