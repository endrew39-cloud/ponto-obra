data class RegistroPonto(
    val cpf: String,
    val nome: String,
    val funcao: String,
    val tipo: String,
    val latitude: Double,
    val longitude: Double,
    val nomeObra: String
) {
    fun paraMapaJson(): Map<String, Any> {
        return mapOf(
            "cpf" to cpf,
            "nome" to nome,
            "funcao" to funcao,
            "tipo" to tipo,
            "latitude" to latitude,
            "longitude" to longitude,
            "nomeObra" to nomeObra
        )
    }
}
