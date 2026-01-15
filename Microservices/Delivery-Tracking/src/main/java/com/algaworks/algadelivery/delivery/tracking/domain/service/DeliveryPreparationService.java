package com.algaworks.algadelivery.delivery.tracking.domain.service;

import com.algaworks.algadelivery.delivery.tracking.api.model.ContactPointRequest;
import com.algaworks.algadelivery.delivery.tracking.api.model.DeliveryRequest;
import com.algaworks.algadelivery.delivery.tracking.api.model.IntemRequest;
import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import com.algaworks.algadelivery.delivery.tracking.domain.model.ContactPoint;
import com.algaworks.algadelivery.delivery.tracking.domain.model.Delivery;
import com.algaworks.algadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DeliveryPreparationService {

    private final DeliveryRepository deliveryRepository;

    private final DeliveryTimeEstimationService deliveryTimeEstimationService;
    private final CourierPayoutCalculationService courierPayoutCalculationService;

    @Transactional
    public Delivery draft(DeliveryRequest request) {
        Delivery delivery = Delivery.draft();
        handlePreparation(request, delivery);
        return deliveryRepository.saveAndFlush(delivery);
    }

    @Transactional
    public Delivery edit(UUID deliveryId, DeliveryRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DomainException());
        delivery.removeItems();
        handlePreparation(request, delivery);
        return deliveryRepository.saveAndFlush(delivery);
    }

    private void handlePreparation(DeliveryRequest request, Delivery delivery) {
        ContactPointRequest senderRequest = request.getSender();
        ContactPointRequest recipientRequest = request.getRecipient();

        ContactPoint sender = ContactPoint.builder()
                .phone(senderRequest.getPhone())
                .name(senderRequest.getName())
                .complement(senderRequest.getComplement())
                .number(senderRequest.getNumber())
                .zipCode(senderRequest.getZipCode())
                .street(senderRequest.getStreet())
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .phone(recipientRequest.getPhone())
                .name(recipientRequest.getName())
                .complement(recipientRequest.getComplement())
                .number(recipientRequest.getNumber())
                .zipCode(recipientRequest.getZipCode())
                .street(recipientRequest.getStreet())
                .build();

        DeliveryEstimate estimate = deliveryTimeEstimationService.estimate(sender, recipient);
        BigDecimal calculatedPayout = courierPayoutCalculationService.calculatePayout(estimate.getDistanceInKm());

        BigDecimal distanceFee = calculateFee(estimate.getDistanceInKm());

        Delivery.PreparationDetails preparationDetails = Delivery.PreparationDetails.builder()
                .recipient(recipient)
                .sender(sender)
                .expectedDeliveryTime(estimate.getEstimatedTime())
                .courierPayout(calculatedPayout)
                .distanceFee(distanceFee)
                .build();

        delivery.editPreparationDetails(preparationDetails);

        for (IntemRequest item : request.getItems()) {
            delivery.addItem(item.getName(), item.getQuantity());
        }
    }

    private BigDecimal calculateFee(Double distanceInKm) {
        return new BigDecimal("3")
                .multiply(new BigDecimal(distanceInKm))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

}
