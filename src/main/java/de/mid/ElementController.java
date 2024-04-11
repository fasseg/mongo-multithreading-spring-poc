package de.mid;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ElementController {

  private final ElementService elementService;

  @Autowired
  public ElementController(ElementService elementService) {
    this.elementService = elementService;
  }

  @GetMapping("/elements")
  public Collection<Element> fetchElements() {
      return this.elementService.fetchAll();
  }
}
