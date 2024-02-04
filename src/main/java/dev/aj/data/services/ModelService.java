package dev.aj.data.services;

import dev.aj.data.domain.model.Model;
import dev.aj.data.domain.repositories.ModelRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;

    public Model persistAModel() {
        Model currentModel = Model.builder()
                                  .uuid(UUID.randomUUID())

                                  .javaUtilDate(new Date())
                                  .javaSqlDate(new java.sql.Date(System.currentTimeMillis()))
                                  .localDateTime(LocalDateTime.now())
                                  .offsetDateTime(OffsetDateTime.now())
                                  .zonedDateTime(ZonedDateTime.now())

                                  .javaUtilDateTZ(new Date())
                                  .javaSqlDateTZ(new java.sql.Date(System.currentTimeMillis()))
                                  .localDateTimeTZ(LocalDateTime.now())
                                  .offsetDateTimeTZ(OffsetDateTime.now())
                                  .zonedDateTimeTZ(ZonedDateTime.now())

                                  .build();
        return modelRepository.save(currentModel);
    }

    public List<Model> getAllModel() {
        return modelRepository.findAll();
    }

    public Model getModelById(Long id) {
        return modelRepository.findById(id).orElse(Model.builder().build());
    }

    public Model persistGivenModel(Model model) {
        return modelRepository.save(model);
    }

    public List<Model> persistGivenModels(List<Model> models) {
        return modelRepository.saveAll(models);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Model updateModel(Long id, Model model) {

        Optional<Model> modelByIdOptional = modelRepository.findById(id);

        if (modelByIdOptional.isPresent()) {
            Model modelById = modelByIdOptional.get();
            modelById.setUuid(model.getUuid());
            modelById.setLocalDateTime(model.getLocalDateTime());
            modelById.setOffsetDateTime(model.getOffsetDateTime());
            modelById.setZonedDateTime(model.getZonedDateTime());
            modelById.setJavaUtilDate(model.getJavaUtilDate());
            modelById.setJavaSqlDate(model.getJavaSqlDate());
            return modelRepository.save(modelById);
        } else {
            throw new NoSuchElementException(STR."""
                    Unable to find \{id} in our records,
                    None of \{model} hasn't been persisted.
                    """);
        }
    }
}
