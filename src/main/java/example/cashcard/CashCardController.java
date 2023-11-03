package example.cashcard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cashcards")
@RequiredArgsConstructor
@Slf4j
public class CashCardController {

    private final CashCardRepository repository;

    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findbyId(@PathVariable Long requestedId, Principal principal) {

        var cc = repository.findByIdAndOwner(requestedId, principal.getName());

        if (cc.isPresent()) {
            return ResponseEntity.ok(cc.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CashCard> create(@RequestBody CashCard cashCard, Principal principal) {

        var cc = new CashCard(null, cashCard.amount(), principal.getName());
        cc = repository.save(cc);
        return ResponseEntity
                .created(URI.create("/cashcards/".concat(cc.id().toString())))
                .build();
    }

    @GetMapping
    public ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {

        Page<CashCard> page = repository.findByOwner(
                principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CashCard cashCard, Principal principal) {

        var card = repository.findByIdAndOwner(id, principal.getName());

        if (card.isPresent()) {
            repository.save(new CashCard(card.get().id(), cashCard.amount(), card.get().owner()));

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {

        if (repository.existsByIdAndOwner(id, principal.getName())) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
