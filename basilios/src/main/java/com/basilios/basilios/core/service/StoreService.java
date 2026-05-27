package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.store.StoreHoursDayDTO;
import com.basilios.basilios.app.dto.store.StoreHoursResponseDTO;
import com.basilios.basilios.app.dto.store.StoreHoursUpdateDTO;
import com.basilios.basilios.app.dto.store.StorePatchUpdateDTO;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Store;
import com.basilios.basilios.core.model.StoreOperatingHour;
import com.basilios.basilios.infra.repository.StoreOperatingHourRepository;
import com.basilios.basilios.infra.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreOperatingHourRepository storeOperatingHourRepository;

    public Store getMainStore() {
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            throw new NotFoundException("Nenhuma loja cadastrada");
        }
        return stores.get(0);
    }

    public List<Store> findAll() {
        return storeRepository.findAll();
    }

    public Store findById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loja não encontrada"));
    }

    public StoreHoursResponseDTO getMainStoreHours() {
        Store store = getMainStore();
        List<StoreOperatingHour> persistedHours = storeOperatingHourRepository.findByStoreId(store.getId());

        Map<DayOfWeek, StoreOperatingHour> byDay = new EnumMap<>(DayOfWeek.class);
        for (StoreOperatingHour hour : persistedHours) {
            byDay.put(hour.getDayOfWeek(), hour);
        }

        List<StoreHoursDayDTO> responseDays = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            StoreOperatingHour persisted = byDay.get(day);
            if (persisted == null) {
                responseDays.add(closedDay(day));
            } else {
                responseDays.add(toDayDTO(persisted));
            }
        }

        responseDays.sort(Comparator.comparingInt(day -> day.getDayOfWeek().getValue()));
        return new StoreHoursResponseDTO(responseDays);
    }

    public StoreHoursDayDTO getMainStoreHoursByDay(DayOfWeek day) {
        return getMainStoreHours().getHours().stream()
                .filter(item -> item.getDayOfWeek() == day)
                .findFirst()
                .orElseGet(() -> closedDay(day));
    }

    @Transactional
    public StoreHoursResponseDTO updateMainStoreHours(StoreHoursUpdateDTO request) {
        Store store = getMainStore();
        validateUniqueDays(request.getHours());

        Map<DayOfWeek, StoreHoursDayDTO> requestByDay = new EnumMap<>(DayOfWeek.class);
        for (StoreHoursDayDTO hourDTO : request.getHours()) {
            requestByDay.put(hourDTO.getDayOfWeek(), hourDTO);
        }

        List<StoreOperatingHour> toPersist = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            StoreHoursDayDTO dto = requestByDay.getOrDefault(day, closedDay(day));

            StoreOperatingHour entity = StoreOperatingHour.builder()
                    .store(store)
                    .dayOfWeek(day)
                    .closed(dto.isClosed())
                    .opensAt(dto.getOpensAt())
                    .closesAt(dto.getClosesAt())
                    .build();
            toPersist.add(entity);
        }

        storeOperatingHourRepository.deleteAllByStoreIdInBulk(store.getId());
        storeOperatingHourRepository.flush();
        List<StoreOperatingHour> saved = storeOperatingHourRepository.saveAll(toPersist);

        List<StoreHoursDayDTO> responseDays = saved.stream()
                .map(this::toDayDTO)
                .sorted(Comparator.comparingInt(day -> day.getDayOfWeek().getValue()))
                .toList();
        return new StoreHoursResponseDTO(responseDays);
    }

    @Transactional
    public Store create(Store store) {
        Store created = storeRepository.save(store);
        if (storeOperatingHourRepository.findByStoreId(created.getId()).isEmpty()) {
            List<StoreOperatingHour> defaultClosedWeek = new ArrayList<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                defaultClosedWeek.add(StoreOperatingHour.builder()
                        .store(created)
                        .dayOfWeek(day)
                        .closed(true)
                        .build());
            }
            storeOperatingHourRepository.saveAll(defaultClosedWeek);
        }
        return created;
    }

    @Transactional
    public Store patchMainStore(StorePatchUpdateDTO patch) {
        Store store = getMainStore();

        if (patch.getName() != null) {
            validateNotBlank("Nome", patch.getName());
            store.setName(patch.getName().trim());
        }
        if (patch.getAddress() != null) {
            validateNotBlank("Endereco", patch.getAddress());
            store.setAddress(patch.getAddress().trim());
        }
        if (patch.getLatitude() != null) {
            store.setLatitude(patch.getLatitude());
        }
        if (patch.getLongitude() != null) {
            store.setLongitude(patch.getLongitude());
        }
        if (patch.getPhone() != null) {
            store.setPhone(patch.getPhone().trim());
        }
        if (patch.getDeliveryFee() != null) {
            store.setDeliveryFee(patch.getDeliveryFee());
        }

        return storeRepository.save(store);
    }

    private void validateNotBlank(String fieldName, String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " nao pode ser vazio");
        }
    }

    private void validateUniqueDays(List<StoreHoursDayDTO> hours) {
        Set<DayOfWeek> seenDays = new HashSet<>();
        for (StoreHoursDayDTO hour : hours) {
            if (!seenDays.add(hour.getDayOfWeek())) {
                throw new IllegalArgumentException("Dia da semana duplicado no payload: " + hour.getDayOfWeek());
            }
        }
    }

    private StoreHoursDayDTO toDayDTO(StoreOperatingHour entity) {
        StoreHoursDayDTO dto = new StoreHoursDayDTO();
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setClosed(entity.isClosed());
        dto.setOpensAt(entity.getOpensAt());
        dto.setClosesAt(entity.getClosesAt());
        return dto;
    }

    private StoreHoursDayDTO closedDay(DayOfWeek day) {
        StoreHoursDayDTO dto = new StoreHoursDayDTO();
        dto.setDayOfWeek(day);
        dto.setClosed(true);
        dto.setOpensAt(null);
        dto.setClosesAt(null);
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        Store store = findById(id);
        storeRepository.delete(store);
    }
}