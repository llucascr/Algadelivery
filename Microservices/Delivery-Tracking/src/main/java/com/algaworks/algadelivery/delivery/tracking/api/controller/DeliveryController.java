package com.algaworks.algadelivery.delivery.tracking.api.controller;

import com.algaworks.algadelivery.delivery.tracking.api.model.CourierIdRequest;
import com.algaworks.algadelivery.delivery.tracking.api.model.DeliveryRequest;
import com.algaworks.algadelivery.delivery.tracking.domain.model.Delivery;
import com.algaworks.algadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import com.algaworks.algadelivery.delivery.tracking.domain.service.DeliveryCheckpointService;
import com.algaworks.algadelivery.delivery.tracking.domain.service.DeliveryPreparationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final DeliveryPreparationService deliveryPreparationService;
    private final DeliveryCheckpointService deliveryCheckpointService;

    private final DeliveryRepository deliveryRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Delivery draft(@RequestBody @Valid DeliveryRequest request) {
        return deliveryPreparationService.draft(request);
    }

    @PutMapping("/{deliveryId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Delivery edit(
            @PathVariable UUID deliveryId,
            @RequestBody @Valid DeliveryRequest request) {
        return deliveryPreparationService.edit(deliveryId, request);
    }

    @GetMapping
    public PagedModel<Delivery> findAll(@PageableDefault Pageable pageable) {
        return new PagedModel<>(deliveryRepository.findAll(pageable));
    }

    @GetMapping("{deliveryId}")
    public Delivery findById(@PathVariable UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{deliveryId}/placement")
    public void place(@PathVariable UUID deliveryId) {
        deliveryCheckpointService.place(deliveryId);
    }

    @PostMapping("/{deliveryId}/pickups")
    public void pickup(
            @Valid @RequestBody CourierIdRequest request,
            @PathVariable UUID deliveryId) {
        deliveryCheckpointService.pickup(request.getCourierId(), deliveryId);
    }

    @PostMapping("/{deliveryId}/completion")
    public void complete(@PathVariable UUID deliveryId) {
        deliveryCheckpointService.complete(deliveryId);
    }



}
