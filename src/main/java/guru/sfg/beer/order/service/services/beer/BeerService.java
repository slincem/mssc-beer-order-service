package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.services.beer.model.BeerDto;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {

    Optional<BeerDto> getBeerServiceInfo(UUID beerId);
    Optional<BeerDto> getBeerServiceInfo(String upc);
}