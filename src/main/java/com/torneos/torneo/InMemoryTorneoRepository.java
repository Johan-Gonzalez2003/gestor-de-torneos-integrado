package com.torneos.torneo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTorneoRepository implements TorneoRepository {

    private final ConcurrentSkipListMap<Long, Torneo> data = new ConcurrentSkipListMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Torneo save(Torneo torneo) {
        long id = torneo.id() != null ? torneo.id() : sequence.incrementAndGet();
        Torneo saved = new Torneo(id, torneo.nombre(), torneo.videojuego(), torneo.fechaInicio(),
                torneo.maxParticipantes(), torneo.estado(), torneo.creadoPor());
        data.put(id, saved);
        return saved;
    }

    @Override
    public Optional<Torneo> findById(long id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Torneo> findPage(int page, int size) {
        return data.values().stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public long count() {
        return data.size();
    }

    @Override
    public void deleteById(long id) {
        data.remove(id);
    }
}
