package dev.aj.data.controllers;

import dev.aj.data.aspects.LogTiming;
import dev.aj.data.domain.model.Model;
import dev.aj.data.services.ModelService;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    @LogTiming(info = "Persist current time", displayPerformanceInTimeUnit = TimeUnit.MICROSECONDS)
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

    @PostMapping("/given/list")
    public ResponseEntity<List<Model>> persistListOfModels(@RequestBody List<Model> models) {
        return ResponseEntity.ok(
                modelService.persistGivenModels(models)
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<Model>> getAllModels() {

        return ResponseEntity.ok(
                modelService.getAllModel()
        );
    }

    @GetMapping("/{id}")
    @LogTiming(info = "Fetch a model given Id", displayPerformanceInTimeUnit = TimeUnit.MICROSECONDS)
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
