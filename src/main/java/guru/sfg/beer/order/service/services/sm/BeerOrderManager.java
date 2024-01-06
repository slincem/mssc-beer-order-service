package guru.sfg.beer.order.service.services.sm;

import guru.sfg.beer.order.service.domain.BeerOrder;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);

}
