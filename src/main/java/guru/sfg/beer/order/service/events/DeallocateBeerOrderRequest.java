package guru.sfg.beer.order.service.events;

import guru.sfg.beer.order.service.web.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeallocateBeerOrderRequest {

    private BeerOrderDto beerOrderDto;
}
