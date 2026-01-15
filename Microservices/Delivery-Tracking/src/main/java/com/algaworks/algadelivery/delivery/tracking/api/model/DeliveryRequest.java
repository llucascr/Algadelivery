package com.algaworks.algadelivery.delivery.tracking.api.model;

import com.algaworks.algadelivery.delivery.tracking.domain.model.ContactPoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeliveryRequest {

    @NotNull
    @Valid
    private ContactPointRequest sender;

    @NotNull
    @Valid
    private ContactPointRequest recipient;

    @NotEmpty
    @Valid
    private List<IntemRequest> items;

}
