package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.services.beer.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@ConfigurationProperties(prefix = "sml.brewery", ignoreUnknownFields = false)
@Component
public class BeerServiceImpl implements BeerService {

    private RestTemplate restTemplate;
    public final static String BEER_SERVICE_PATH = "/api/v1/beer";
    public final static String BEER_SERVICE_UPC_PATH = BEER_SERVICE_PATH + "/upc/";

    private String beerServiceHost;

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }

    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Optional<BeerDto> getBeerServiceInfo(UUID beerId) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_SERVICE_PATH + "/" + beerId.toString(), BeerDto.class));
    }

    @Override
    public Optional<BeerDto> getBeerServiceInfo(String upc) {
        log.debug("Calling Beer Service: {}", upc);
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_SERVICE_UPC_PATH + upc, BeerDto.class));
    }
}
