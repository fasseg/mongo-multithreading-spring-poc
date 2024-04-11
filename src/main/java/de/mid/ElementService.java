package de.mid;

import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ElementService {

  private final MongoTemplate template;

  private final ExecutorService executor = Executors.newFixedThreadPool(12);

  public ElementService(MongoTemplate template) {
    this.template = template;
  }

  @PostConstruct
  public void init() {
    for (int i = 0; i < 100; i++) {
      final Element e = new Element();
      e.setData(RandomStringUtils.randomAlphabetic(17));
      this.template.save(e);
    }
  }


  public Collection<Element> fetchAll() {
    final Collection<Future<List<Element>>> futures = new HashSet<>();
    for (char ch = 'a'; ch <= 'z'; ch++) {
      futures.add(this.executor.submit(new MongoCallable(ch, this.template)));
    }

    final Collection<Element> elements = new HashSet<>();
    for (final Future<List<Element>> future : futures) {
      try {
        elements.addAll(future.get());
      } catch (final InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
    return elements;
  }

  private static class MongoCallable implements Callable<List<Element>> {

    private final Character ch;

    private final MongoTemplate template;

    private MongoCallable(final Character ch, final MongoTemplate template) {
      this.ch = ch;
      this.template = template;
    }

    @Override
    public List<Element> call() throws Exception {
      final Query q = new Query(Criteria.where("data").regex("^" + ch, "i"));
      return template.find(q, Element.class);
    }
  }

}
