package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.web.model.CustomerPagedList;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerPagedList listCustomers(Pageable pageable);
}
