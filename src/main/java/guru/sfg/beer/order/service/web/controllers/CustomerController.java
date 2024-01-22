package guru.sfg.beer.order.service.web.controllers;

import guru.sfg.beer.order.service.services.CustomerService;
import guru.sfg.beer.order.service.web.model.CustomerPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/")
@RestController
public class CustomerController {

    private static final String DEFAULT_PAGE_NUMBER = "0";
    private static final String DEFAULT_PAGE_SIZE = "25";

    private final CustomerService customerService;

    @GetMapping
    public CustomerPagedList listCustomers(@RequestParam(value = "pageNumber", required = false, defaultValue = DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                           @RequestParam(value = "pageSize", required = false, defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize) {

        return customerService.listCustomers(PageRequest.of(pageNumber, pageSize));
    }
}
