package io.github.sweatunipd.NearYou.infrastracture.adapters.input.web;

import io.github.sweatunipd.NearYou.application.ports.input.CreateRentUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RentCreationController {

    public final CreateRentUseCase createRentUseCase;

    public RentCreationController(CreateRentUseCase createRentUseCase) {
        this.createRentUseCase = createRentUseCase;
    }

    @GetMapping("/rent/{id}")
    public int createRent(@PathVariable int id) {
        createRentUseCase.createRent(id);
        return 1;
    }
}
