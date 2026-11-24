
package com.example.huertoHogar_Backend.boleta;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOLETA")
public class BoletaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOLETA_ID")
    private Long boletaId;

    @Column(name = "CODIGO", nullable = false, unique = true)
    private String codigo;

    @Column(name = "USUARIO_ID")
    private Long usuarioId;

    @Column(name = "FECHA_BOLETA", insertable = false, updatable = false)
    private LocalDateTime fechaBoleta;

    @Column(name = "ESTADO", nullable = false)
    private String estado;

    @Column(name = "NOMBRE_CLIENTE", nullable = false)
    private String nombreCliente;

    @Column(name = "APELLIDOS_CLIENTE", nullable = false)
    private String apellidosCliente;

    @Column(name = "CORREO_CLIENTE", nullable = false)
    private String correoCliente;

    @Column(name = "CALLE", nullable = false)
    private String calle;

    @Column(name = "DEPARTAMENTO")
    private String departamento;

    @Column(name = "REGION", nullable = false)
    private String region;

    @Column(name = "COMUNA", nullable = false)
    private String comuna;

    @Column(name = "INDICACIONES")
    private String indicaciones;

    @Column(name = "TIPO_ENTREGA", nullable = false)
    private String tipoEntrega;

    @Column(name = "METODO_PAGO", nullable = false)
    private String metodoPago;

    @Column(name = "SUBTOTAL", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "COSTO_ENVIO", nullable = false)
    private BigDecimal costoEnvio;

    @Column(name = "TOTAL", nullable = false)
    private BigDecimal total;

    public BoletaModel() {
    }

    public Long getBoletaId() {
        return boletaId;
    }

    public void setBoletaId(Long boletaId) {
        this.boletaId = boletaId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFechaBoleta() {
        return fechaBoleta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getApellidosCliente() {
        return apellidosCliente;
    }

    public void setApellidosCliente(String apellidosCliente) {
        this.apellidosCliente = apellidosCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
