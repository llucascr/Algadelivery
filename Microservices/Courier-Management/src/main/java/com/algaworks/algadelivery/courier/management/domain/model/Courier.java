package com.algaworks.algadelivery.courier.management.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Courier {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Setter(AccessLevel.PUBLIC)
    private String name;

    @Setter(AccessLevel.PUBLIC)
    private String phone;

    private Integer fulfilledDeliveriesQuantity;

    private Integer pedingDeliveriesQuantity;

    private OffsetDateTime lastFulfilledDeliveryAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "courier")
    private List<AssignedDelivery> pedingDeliveries = new ArrayList<>();

    public List<AssignedDelivery> getPedingDeliveries() {
        return Collections.unmodifiableList(this.pedingDeliveries);
    }

    public static Courier brandNew(String name, String phone) {
        Courier courier = new Courier();
        courier.setId(UUID.randomUUID());
        courier.setName(name);
        courier.setPhone(phone);
        courier.setPedingDeliveriesQuantity(0);
        courier.setFulfilledDeliveriesQuantity(0);
        return courier;
    }

    public void assign(UUID deliveryId) {
        this.pedingDeliveries.add(AssignedDelivery.peding(deliveryId, this));
        this.pedingDeliveriesQuantity++;
    }

    public void fulfill(UUID deliveryId) {
        AssignedDelivery delivery = this.pedingDeliveries.stream().filter(
                d -> d.getId().equals(deliveryId)
                ).findFirst().orElseThrow();
        this.pedingDeliveries.remove(delivery);

        this.pedingDeliveriesQuantity--;
        this.fulfilledDeliveriesQuantity++;
        this.lastFulfilledDeliveryAt = OffsetDateTime.now();
    }


}
