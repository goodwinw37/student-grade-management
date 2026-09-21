package uk.ac.ucl.comp0010.controller;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ucl.comp0010.dto.ModuleRequest;
import uk.ac.ucl.comp0010.model.Module;
import uk.ac.ucl.comp0010.repository.ModuleRepository;

/**
 * CRUD-style operations for modules so demos can seed new records without editing SQL files.
 */
@RestController
@RequestMapping("/management/modules")
@Validated
public class ModuleController {

  private final ModuleRepository moduleRepository;

  public ModuleController(ModuleRepository moduleRepository) {
    this.moduleRepository = moduleRepository;
  }

  /**
   * Lists modules ordered by module code.
   */
  @GetMapping
  public List<Module> listModules() {
    return StreamSupport.stream(moduleRepository.findAll().spliterator(), false)
        .sorted(Comparator.comparing(Module::getCode))
        .collect(Collectors.toList());
  }

  /**
   * Creates a module with validation to prevent duplicates.
   */
  @PostMapping
  public ResponseEntity<Module> createModule(@Valid @RequestBody ModuleRequest request) {
    String code = request.code().trim().toUpperCase();
    String name = request.name().trim();
    if (moduleRepository.existsById(code)) {
      throw new IllegalArgumentException("Module code '%s' already exists".formatted(code));
    }
    if (moduleRepository.existsByName(name)) {
      throw new IllegalArgumentException("Module name '%s' already exists".formatted(name));
    }

    Module module = new Module(code, name, request.mnc());
    Module saved = moduleRepository.save(module);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }
}