package dev.aj.data.controllers;

import dev.aj.data.domain.model.Model;
import dev.aj.data.services.ModelService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/timezone")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @PostMapping("/current")
    public ResponseEntity<Model> persistCurrentModel() {
        return ResponseEntity.ok(
                modelService.persistAModel()
        );
    }

    @PostMapping("/given")
    public ResponseEntity<Model> persistGivenModel(@RequestBody Model model) {
        return ResponseEntity.ok(
                modelService.persistGivenModel(model)
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<Model>> getAllModels() {

        return ResponseEntity.ok(
                modelService.getAllModel()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Model> getAModel(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(
                modelService.getModelById(id)
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Model> updateAnExistingModel(@PathVariable(value = "id") Long id, @RequestBody Model model) {
        return ResponseEntity.ok(
                modelService.updateModel(id, model)
        );
    }
}
