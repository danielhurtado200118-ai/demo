package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository repository;
    private final AuditoriaRepository auditoriaRepository;

    public ProductoController(ProductoRepository repository, AuditoriaRepository auditoriaRepository) {
        this.repository = repository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping
    public List<Producto> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Producto crear(@RequestBody Producto producto) {
        Producto guardado = repository.save(producto);

        // RF-06: Registro de creación
        registrarAuditoria("PRODUCTO_CREADO", "Se creó: " + guardado.getNombre());
        
        return guardado;
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto detalles) {
        Producto p = repository.findById(id).orElseThrow();
        p.setNombre(detalles.getNombre());
        p.setCantidad(detalles.getCantidad());
        p.setPrecio(detalles.getPrecio());
        p.setDescripcion(detalles.getDescripcion());
        
        Producto actualizado = repository.save(p);
        
        // RF-06: Registro de edición (Seguridad extra para Fase 2)
        registrarAuditoria("PRODUCTO_EDITADO", "Se editó el ID: " + id);
        
        return actualizado;
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        repository.deleteById(id);
        
        // RF-06: Registro de eliminación (¡Vital para el Red Team!)
        registrarAuditoria("PRODUCTO_ELIMINADO", "Se borró el ID: " + id);
    }

    // Método auxiliar para no repetir código de auditoría
    private void registrarAuditoria(String evento, String detalles) {
        Auditoria log = new Auditoria();
        log.setEvento(evento);
        log.setUsuario("Admin (Sistema)"); 
        log.setDetalles(detalles);
        log.setFechaHora(LocalDateTime.now());
        auditoriaRepository.save(log);
    }
}