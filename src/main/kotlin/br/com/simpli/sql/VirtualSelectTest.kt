package br.com.simpli.sql

import org.junit.Assert.*
import org.junit.Test
import java.sql.ResultSet
import java.util.*
import java.text.SimpleDateFormat



class VirtualSelectTest {

    var dtFormat = SimpleDateFormat( "yyyy-MM-dd")
    val expectedPrincipalSelectFields = "principal.idPrincipalPk,principal.textoObrigatorio,principal.textoFacultativo,principal.decimalObrigatorio,principal.decimalFacultativo,principal.inteiroObrigatorio,principal.inteiroFacultativo,principal.booleanoObrigatorio,principal.booleanoFacultativo,principal.dataObrigatoria,principal.dataFacultativa,principal.datahoraObrigatoria,principal.datahoraFacultativa,principal.ativo,principal.email,principal.urlImagem,principal.url,principal.idGrupoDoPrincipalFk,principal.idGrupoDoPrincipalFacultativoFk,principal.unico,principal.dataCriacao,principal.dataAlteracao,principal.nome,principal.titulo,principal.cpf,principal.cnpj,principal.rg,principal.celular,principal.textoGrande,principal.snake_case,principal.preco"
    val expectedGrupoDoPrincipalSelectFields = "grupo_do_principal.idGrupoDoPrincipalPk,grupo_do_principal.titulo"
    val expectedPrincipalInsertFields = "principal.textoObrigatorio,principal.textoFacultativo,principal.decimalObrigatorio,principal.decimalFacultativo,principal.inteiroObrigatorio,principal.inteiroFacultativo,principal.booleanoObrigatorio,principal.booleanoFacultativo,principal.dataObrigatoria,principal.dataFacultativa,principal.datahoraObrigatoria,principal.datahoraFacultativa,principal.ativo,principal.email,principal.senha,principal.urlImagem,principal.url,principal.idGrupoDoPrincipalFk,principal.idGrupoDoPrincipalFacultativoFk,principal.unico,principal.dataCriacao,principal.nome,principal.titulo,principal.cpf,principal.cnpj,principal.rg,principal.celular,principal.textoGrande,principal.snake_case,principal.preco"

    @Test
    fun getOne() {
        val principalRm = PrincipalRM()

        val vs = VirtualSelect()
                .selectFields(principalRm.selectFields)
                .from(principalRm)
                .whereEq(principalRm.idPrincipalPk, 123)

        assertEquals(" SELECT $expectedPrincipalSelectFields  FROM principal  WHERE (principal.idPrincipalPk = 123) ",
                vs.toString())
    }

    @Test
    fun getList() {
        val filter = PrincipalListFilter().apply {
            orderBy = "inteiroObrigatorio"
            ascending = false
            query = "lorem"
            startDataCriacao = dtFormat.parse("2018-09-09")
            endDataCriacao = dtFormat.parse("2018-09-13")
            minDecimalObrigatorio = 1.1
            maxDecimalObrigatorio = 5.2
            idGrupoDoPrincipalFk = listOf(1, 2, 3)
        }

        val principalRm = PrincipalRM()
        val grupoDoPrincipalRm = GrupoDoPrincipalRM()
        val grupoDoPrincipalFacultativoRm = GrupoDoPrincipalRM("grupo_do_principal_facultativo")

        val vs = VirtualSelect()
                .groupBy(principalRm.idPrincipalPk)
                .selectFields(principalRm.selectFields)
                .from(principalRm)
                .selectFields(grupoDoPrincipalRm.selectFields)
                .innerJoin(grupoDoPrincipalRm, grupoDoPrincipalRm.idGrupoDoPrincipalPk, principalRm.idGrupoDoPrincipalFk)
                .selectFields(grupoDoPrincipalFacultativoRm.selectFields)
                .leftJoin(grupoDoPrincipalFacultativoRm, grupoDoPrincipalFacultativoRm.idGrupoDoPrincipalPk, principalRm.idGrupoDoPrincipalFacultativoFk)
                .wherePrincipalFilter(principalRm, filter)
                .orderAndLimitPrincipal(principalRm, filter)
                .whereSome {
                    whereAll {
                        whereGt(principalRm.decimalObrigatorio, 2)
                        whereLt(principalRm.decimalFacultativo, 3)
                    }
                    whereDateGt(principalRm.dataObrigatoria, dtFormat.parse("2019-09-09"))
                    whereDateLt(principalRm.dataFacultativa, dtFormat.parse("2020-09-09"))
                }
                .whereNotEq(principalRm.booleanoFacultativo, true)
                .whereDateEq(principalRm.dataCriacao, dtFormat.parse("2020-01-01"))
                .whereNull(principalRm.inteiroFacultativo)
                .whereNotNull(principalRm.textoFacultativo)
                .whereNotIn(principalRm.idPrincipalPk, 2, 3, 4, 6, 8)
                .whereBetween(principalRm.preco, 8, 80)

        assertEquals(" SELECT $expectedPrincipalSelectFields,$expectedGrupoDoPrincipalSelectFields,${expectedGrupoDoPrincipalSelectFields.replace("grupo_do_principal", "grupo_do_principal_facultativo")} " +
                " FROM principal " +
                " INNER JOIN grupo_do_principal  ON grupo_do_principal.idGrupoDoPrincipalPk = principal.idGrupoDoPrincipalFk " +
                " LEFT JOIN grupo_do_principal  AS grupo_do_principal_facultativo ON grupo_do_principal_facultativo.idGrupoDoPrincipalPk = principal.idGrupoDoPrincipalFacultativoFk " +
                " WHERE (DATE(principal.dataCriacao) >= DATE(\"2018-09-09 00:00:00\")) " +
                " AND (DATE(principal.dataCriacao) <= DATE(\"2018-09-13 00:00:00\")) " +
                " AND (principal.decimalObrigatorio >= 1.1) " +
                " AND (principal.decimalObrigatorio <= 5.2) " +
                " AND (principal.ativo = true) " +
                " AND (principal.booleanoFacultativo != true) " +
                " AND (DATE(principal.dataCriacao) = DATE(\"2020-01-01 00:00:00\")) " +
                " AND (principal.inteiroFacultativo IS NULL) " +
                " AND (principal.textoFacultativo IS NOT NULL) " +
                " AND (principal.idGrupoDoPrincipalFk IN (1,2,3)) " +
                " AND (principal.idPrincipalPk NOT IN (2,3,4,6,8)) " +
                " AND (principal.preco BETWEEN 8 AND 80) " +
                " AND (    (principal.idPrincipalPk LIKE \"%lorem%\")  OR (principal.textoObrigatorio LIKE \"%lorem%\")  OR (principal.textoFacultativo LIKE \"%lorem%\")  OR (principal.email LIKE \"%lorem%\")  OR (principal.unico LIKE \"%lorem%\")  OR (principal.nome LIKE \"%lorem%\")  OR (principal.titulo LIKE \"%lorem%\")  OR (principal.cpf LIKE \"%lorem%\")  OR (principal.cnpj LIKE \"%lorem%\")  OR (principal.rg LIKE \"%lorem%\")  OR (principal.celular LIKE \"%lorem%\")  OR (principal.textoGrande LIKE \"%lorem%\")  OR (principal.snake_case LIKE \"%lorem%\")   ) " +
                " AND (   " +
                    " (DATE(principal.dataObrigatoria) > DATE(\"2019-09-09 00:00:00\")) " +
                    " OR (DATE(principal.dataFacultativa) < DATE(\"2020-09-09 00:00:00\")) " +
                    " OR (   " +
                        " (principal.decimalObrigatorio > 2) " +
                        " AND (principal.decimalFacultativo < 3) " +
                    "  ) " +
                "  ) " +
                " GROUP BY principal.idPrincipalPk " +
                " ORDER BY principal.inteiroObrigatorio DESC ",
                vs.toString())
    }

    @Test
    fun count() {
        val filter = PrincipalListFilter()

        val principalRm = PrincipalRM()
        val vs = VirtualSelect()
                .countField(principalRm.idPrincipalPk)
                .from(principalRm)
                .wherePrincipalFilter(principalRm, filter)

        assertEquals(" SELECT COUNT(principal.idPrincipalPk)  FROM principal  WHERE (principal.ativo = true) ", vs.toString())
    }

    @Test
    fun update() {
        val principal = Principal().apply {
            idPrincipalPk = 1
            textoObrigatorio = "1"
            decimalObrigatorio = 1.0
            inteiroObrigatorio = 1
            booleanoObrigatorio = true
            ativo = true
            idGrupoDoPrincipalFk = 1
            unico = "1"
        }

        val principalRm = PrincipalRM()
        val query = Query()
                .updateTable(principalRm.table)
                .updateSet(principalRm.updateSet(principal))
                .whereEq(principalRm.idPrincipalPk.column, principal.idPrincipalPk)

        assertEquals(" UPDATE principal  SET " +
                "principal.textoObrigatorio = \"1\", " +
                "principal.textoFacultativo = null, " +
                "principal.decimalObrigatorio = 1.0, " +
                "principal.decimalFacultativo = null, " +
                "principal.inteiroObrigatorio = 1, " +
                "principal.inteiroFacultativo = null, " +
                "principal.booleanoObrigatorio = true, " +
                "principal.booleanoFacultativo = null, " +
                "principal.dataObrigatoria = null, " +
                "principal.dataFacultativa = null, " +
                "principal.datahoraObrigatoria = null, " +
                "principal.datahoraFacultativa = null, " +
                "principal.ativo = true, " +
                "principal.email = null, " +
                "principal.senha = null, " +
                "principal.urlImagem = null, " +
                "principal.url = null, " +
                "principal.idGrupoDoPrincipalFk = 1, " +
                "principal.idGrupoDoPrincipalFacultativoFk = null, " +
                "principal.unico = \"1\", " +
                "principal.dataAlteracao = null, " +
                "principal.nome = null, " +
                "principal.titulo = null, " +
                "principal.cpf = null, " +
                "principal.cnpj = null, " +
                "principal.rg = null, " +
                "principal.celular = null, " +
                "principal.textoGrande = null, " +
                "principal.snake_case = null, " +
                "principal.preco = null " +
                " WHERE (idPrincipalPk = 1) ",
                query.toString())
    }

    @Test
    fun insert() {
        val principal = Principal().apply {
            textoObrigatorio = "1"
            decimalObrigatorio = 1.0
            inteiroObrigatorio = 1
            booleanoObrigatorio = true
            ativo = true
            idGrupoDoPrincipalFk = 1
            unico = "1"
        }

        val principalRm = PrincipalRM()
        val query = Query()
                .insertInto(principalRm.table)
                .insertValues(principalRm.insertValues(principal))

        assertEquals(" INSERT INTO principal  ($expectedPrincipalInsertFields) VALUES (\"1\",null,1.0,null,1,null,true,null,null,null,null,null,true,null,null,null,null,1,null,\"1\",null,null,null,null,null,null,null,null,null,null) ", query.toString())
    }

    @Test
    fun exist() {
        val principalRm = PrincipalRM()
        val vs = VirtualSelect()
                .select(principalRm.idPrincipalPk)
                .from(principalRm)
                .whereEq(principalRm.idPrincipalPk, 123)

        assertEquals(" SELECT principal.idPrincipalPk  FROM principal  WHERE (principal.idPrincipalPk = 123) ", vs.toString())
    }

    @Test
    fun existUnico() {
        val principalRm = PrincipalRM()
        val vs = VirtualSelect()
                .select(principalRm.unico)
                .from(principalRm)
                .whereEq(principalRm.unico, "aeiou")
                .whereEq(principalRm.idPrincipalPk, 123)

        assertEquals(" SELECT principal.unico  FROM principal  WHERE (principal.unico = \"aeiou\")  AND (principal.idPrincipalPk = 123) ", vs.toString())
    }

    @Test
    fun softDelete() {
        val principalRm = PrincipalRM()
        val query = Query()
                .updateTable(principalRm.table)
                .updateSet(principalRm.ativo.column to false)
                .whereEq(principalRm.idPrincipalPk.column, 123)

        assertEquals(" UPDATE principal  SET ativo = false  WHERE (idPrincipalPk = 123) ", query.toString())
    }

    @Test
    fun innerQuery() {
        val principalRm = PrincipalRM()
        val vs = VirtualSelect()
                .select(principalRm.idPrincipalPk)
                .from(Query("SELECT * FROM principal"), "myinner")
                .whereEq(principalRm.idPrincipalPk, 123)

        assertEquals(" SELECT principal.idPrincipalPk  FROM (   SELECT * FROM principal   ) myinner  WHERE (principal.idPrincipalPk = 123) ", vs.toString())
    }

    private fun VirtualSelect.wherePrincipalFilter(principalRm: PrincipalRM, filter: PrincipalListFilter): VirtualSelect {
        whereEq(principalRm.ativo, true)

        filter.query?.also {
            if (it.isNotEmpty()) {
                whereSomeLikeThis(principalRm.fieldsToSearch, "%$it%")
            }
        }

        filter.idGrupoDoPrincipalFk?.also {
            if (it.isNotEmpty()) {
                whereIn(principalRm.idGrupoDoPrincipalFk, *it.toTypedArray())
            }
        }

        filter.idGrupoDoPrincipalFacultativoFk?.also {
            if (it.isNotEmpty()) {
                whereIn(principalRm.idGrupoDoPrincipalFacultativoFk, *it.toTypedArray())
            }
        }

        filter.startDataObrigatoria?.also {
            whereDateGtEq(principalRm.dataObrigatoria, it)
        }
        filter.endDataObrigatoria?.also {
            whereDateLtEq(principalRm.dataObrigatoria, it)
        }

        filter.startDataFacultativa?.also {
            whereDateGtEq(principalRm.dataFacultativa, it)
        }
        filter.endDataFacultativa?.also {
            whereDateLtEq(principalRm.dataFacultativa, it)
        }

        filter.startDatahoraObrigatoria?.also {
            whereDateGtEq(principalRm.datahoraObrigatoria, it)
        }
        filter.endDatahoraObrigatoria?.also {
            whereDateLtEq(principalRm.datahoraObrigatoria, it)
        }

        filter.startDatahoraFacultativa?.also {
            whereDateGtEq(principalRm.datahoraFacultativa, it)
        }
        filter.endDatahoraFacultativa?.also {
            whereDateLtEq(principalRm.datahoraFacultativa, it)
        }

        filter.startDataCriacao?.also {
            whereDateGtEq(principalRm.dataCriacao, it)
        }
        filter.endDataCriacao?.also {
            whereDateLtEq(principalRm.dataCriacao, it)
        }

        filter.startDataAlteracao?.also {
            whereDateGtEq(principalRm.dataAlteracao, it)
        }
        filter.endDataAlteracao?.also {
            whereDateLtEq(principalRm.dataAlteracao, it)
        }

        filter.minDecimalObrigatorio?.also {
            whereGtEq(principalRm.decimalObrigatorio, it)
        }
        filter.maxDecimalObrigatorio?.also {
            whereLtEq(principalRm.decimalObrigatorio, it)
        }

        filter.minDecimalFacultativo?.also {
            whereGtEq(principalRm.decimalFacultativo, it)
        }
        filter.maxDecimalFacultativo?.also {
            whereLtEq(principalRm.decimalFacultativo, it)
        }

        filter.minInteiroObrigatorio?.also {
            whereGtEq(principalRm.inteiroObrigatorio, it)
        }
        filter.maxInteiroObrigatorio?.also {
            whereLtEq(principalRm.inteiroObrigatorio, it)
        }

        filter.minInteiroFacultativo?.also {
            whereGtEq(principalRm.inteiroFacultativo, it)
        }
        filter.maxInteiroFacultativo?.also {
            whereLtEq(principalRm.inteiroFacultativo, it)
        }

        filter.minPreco?.also {
            whereGtEq(principalRm.preco, it)
        }
        filter.maxPreco?.also {
            whereLtEq(principalRm.preco, it)
        }

        filter.booleanoObrigatorio?.also {
            whereEq(principalRm.booleanoObrigatorio, it)
        }

        filter.booleanoFacultativo?.also {
            whereEq(principalRm.booleanoFacultativo, it)
        }

        return this
    }

    private fun VirtualSelect.orderAndLimitPrincipal(principalRm: PrincipalRM, filter: PrincipalListFilter): VirtualSelect {
        orderBy(principalRm.orderMap, filter.orderBy to filter.ascending)

        limitByPage(filter.page, filter.limit)

        return this
    }
}

private class Principal {
    var idPrincipalPk: Long = 0

    var grupoDoPrincipal: GrupoDoPrincipal? = null
    var grupoDoPrincipalFacultativo: GrupoDoPrincipal? = null

    var textoObrigatorio: String? = null
    var unico: String? = null

    var decimalObrigatorio: Double? = null
    var inteiroObrigatorio: Long? = null
    var booleanoObrigatorio: Boolean? = null
    var dataObrigatoria: Date? = null
    var datahoraObrigatoria: Date? = null
    var ativo: Boolean? = null
    var dataCriacao: Date? = null

    var textoFacultativo: String? = null
    var email: String? = null
    var urlImagem: String? = null
    var url: String? = null
    var nome: String? = null
    var titulo: String? = null
    var cpf: String? = null
    var cnpj: String? = null
    var rg: String? = null
    var celular: String? = null
    var textoGrande: String? = null
    var snakeCase: String? = null

    var decimalFacultativo: Double? = null
    var inteiroFacultativo: Long? = null
    var booleanoFacultativo: Boolean? = null
    var dataFacultativa: Date? = null
    var datahoraFacultativa: Date? = null
    var dataAlteracao: Date? = null
    var preco: Double? = null

    var senha: String? = null

    var idGrupoDoPrincipalFk: Long
        get() = grupoDoPrincipal?.idGrupoDoPrincipalPk ?: 0
        set(value) {
            if (value == 0L) {
                grupoDoPrincipal = null
                return
            }
            if (grupoDoPrincipal == null) {
                grupoDoPrincipal = GrupoDoPrincipal()
            }
            grupoDoPrincipal?.idGrupoDoPrincipalPk = value
        }

    var idGrupoDoPrincipalFacultativoFk: Long?
        get() = grupoDoPrincipalFacultativo?.idGrupoDoPrincipalPk
        set(value) {
            if (value == null || value == 0L) {
                grupoDoPrincipalFacultativo = null
                return
            }
            if (grupoDoPrincipalFacultativo == null) {
                grupoDoPrincipalFacultativo = GrupoDoPrincipal()
            }
            grupoDoPrincipalFacultativo?.idGrupoDoPrincipalPk = value
        }
}

private class GrupoDoPrincipal {
    var idGrupoDoPrincipalPk: Long = 0
    var titulo: String? = null
}

private class PrincipalRM(override var alias: String? = null) : RelationalMapper<Principal>() {
    override val table = "principal"

    val idPrincipalPk = col("idPrincipalPk",
            { idPrincipalPk },
            { idPrincipalPk = it.value() })

    val textoObrigatorio = col("textoObrigatorio",
            { textoObrigatorio },
            { textoObrigatorio = it.value() })

    val textoFacultativo = col("textoFacultativo",
            { textoFacultativo },
            { textoFacultativo = it.value() })

    val decimalObrigatorio = col("decimalObrigatorio",
            { decimalObrigatorio },
            { decimalObrigatorio = it.value() })

    val decimalFacultativo = col("decimalFacultativo",
            { decimalFacultativo },
            { decimalFacultativo = it.value() })

    val inteiroObrigatorio = col("inteiroObrigatorio",
            { inteiroObrigatorio },
            { inteiroObrigatorio = it.value() })

    val inteiroFacultativo = col("inteiroFacultativo",
            { inteiroFacultativo },
            { inteiroFacultativo = it.value() })

    val booleanoObrigatorio = col("booleanoObrigatorio",
            { booleanoObrigatorio },
            { booleanoObrigatorio = it.value() })

    val booleanoFacultativo = col("booleanoFacultativo",
            { booleanoFacultativo },
            { booleanoFacultativo = it.value() })

    val dataObrigatoria = col("dataObrigatoria",
            { dataObrigatoria },
            { dataObrigatoria = it.value() })

    val dataFacultativa = col("dataFacultativa",
            { dataFacultativa },
            { dataFacultativa = it.value() })

    val datahoraObrigatoria = col("datahoraObrigatoria",
            { datahoraObrigatoria },
            { datahoraObrigatoria = it.value() })

    val datahoraFacultativa = col("datahoraFacultativa",
            { datahoraFacultativa },
            { datahoraFacultativa = it.value() })

    val ativo = col("ativo",
            { ativo },
            { ativo = it.value() })

    val email = col("email",
            { email },
            { email = it.value() })

    val senha = col("senha",
            { senha },
            { senha = it.value() })

    val urlImagem = col("urlImagem",
            { urlImagem },
            { urlImagem = it.value() })

    val url = col("url",
            { url },
            { url = it.value() })

    val idGrupoDoPrincipalFk = col("idGrupoDoPrincipalFk",
            { idGrupoDoPrincipalFk },
            { idGrupoDoPrincipalFk = it.value() })

    val idGrupoDoPrincipalFacultativoFk = col("idGrupoDoPrincipalFacultativoFk",
            { idGrupoDoPrincipalFacultativoFk },
            { idGrupoDoPrincipalFacultativoFk = it.value() })

    val unico = col("unico",
            { unico },
            { unico = it.value() })

    val dataCriacao = col("dataCriacao",
            { dataCriacao },
            { dataCriacao = it.value() })

    val dataAlteracao = col("dataAlteracao",
            { dataAlteracao },
            { dataAlteracao = it.value() })

    val nome = col("nome",
            { nome },
            { nome = it.value() })

    val titulo = col("titulo",
            { titulo },
            { titulo = it.value() })

    val cpf = col("cpf",
            { cpf },
            { cpf = it.value() })

    val cnpj = col("cnpj",
            { cnpj },
            { cnpj = it.value() })

    val rg = col("rg",
            { rg },
            { rg = it.value() })

    val celular = col("celular",
            { celular },
            { celular = it.value() })

    val textoGrande = col("textoGrande",
            { textoGrande },
            { textoGrande = it.value() })

    val snakeCase = col("snake_case",
            { snakeCase },
            { snakeCase = it.value() })

    val preco = col("preco",
            { preco },
            { preco = it.value() })


    fun build(rs: ResultSet) = Principal().apply {
        selectFields.forEach { col ->
            col.build(this, rs)
        }
    }

    val selectFields
        get() = arrayOf(
                idPrincipalPk,
                textoObrigatorio,
                textoFacultativo,
                decimalObrigatorio,
                decimalFacultativo,
                inteiroObrigatorio,
                inteiroFacultativo,
                booleanoObrigatorio,
                booleanoFacultativo,
                dataObrigatoria,
                dataFacultativa,
                datahoraObrigatoria,
                datahoraFacultativa,
                ativo,
                email,
                urlImagem,
                url,
                idGrupoDoPrincipalFk,
                idGrupoDoPrincipalFacultativoFk,
                unico,
                dataCriacao,
                dataAlteracao,
                nome,
                titulo,
                cpf,
                cnpj,
                rg,
                celular,
                textoGrande,
                snakeCase,
                preco
        )

    val fieldsToSearch
        get() = arrayOf(
                idPrincipalPk,
                textoObrigatorio,
                textoFacultativo,
                email,
                unico,
                nome,
                titulo,
                cpf,
                cnpj,
                rg,
                celular,
                textoGrande,
                snakeCase
        )

    val orderMap
        get() = mapOf(
                "grupoDoPrincipal" to idGrupoDoPrincipalFk,
                "grupoDoPrincipalFacultativo" to idGrupoDoPrincipalFacultativoFk,
                "idPrincipalPk" to idPrincipalPk,
                "textoObrigatorio" to textoObrigatorio,
                "textoFacultativo" to textoFacultativo,
                "decimalObrigatorio" to decimalObrigatorio,
                "decimalFacultativo" to decimalFacultativo,
                "inteiroObrigatorio" to inteiroObrigatorio,
                "inteiroFacultativo" to inteiroFacultativo,
                "booleanoObrigatorio" to booleanoObrigatorio,
                "booleanoFacultativo" to booleanoFacultativo,
                "dataObrigatoria" to dataObrigatoria,
                "dataFacultativa" to dataFacultativa,
                "datahoraObrigatoria" to datahoraObrigatoria,
                "datahoraFacultativa" to datahoraFacultativa,
                "ativo" to ativo,
                "email" to email,
                "urlImagem" to urlImagem,
                "url" to url,
                "unico" to unico,
                "dataCriacao" to dataCriacao,
                "dataAlteracao" to dataAlteracao,
                "nome" to nome,
                "titulo" to titulo,
                "cpf" to cpf,
                "cnpj" to cnpj,
                "rg" to rg,
                "celular" to celular,
                "textoGrande" to textoGrande,
                "snakeCase" to snakeCase,
                "preco" to preco
        )

    fun updateSet(principal: Principal) = colsToMap(principal,
            textoObrigatorio,
            textoFacultativo,
            decimalObrigatorio,
            decimalFacultativo,
            inteiroObrigatorio,
            inteiroFacultativo,
            booleanoObrigatorio,
            booleanoFacultativo,
            dataObrigatoria,
            dataFacultativa,
            datahoraObrigatoria,
            datahoraFacultativa,
            ativo,
            email,
            senha,
            urlImagem,
            url,
            idGrupoDoPrincipalFk,
            idGrupoDoPrincipalFacultativoFk,
            unico,
            dataAlteracao,
            nome,
            titulo,
            cpf,
            cnpj,
            rg,
            celular,
            textoGrande,
            snakeCase,
            preco
    )

    fun insertValues(principal: Principal) = colsToMap(principal,
            textoObrigatorio,
            textoFacultativo,
            decimalObrigatorio,
            decimalFacultativo,
            inteiroObrigatorio,
            inteiroFacultativo,
            booleanoObrigatorio,
            booleanoFacultativo,
            dataObrigatoria,
            dataFacultativa,
            datahoraObrigatoria,
            datahoraFacultativa,
            ativo,
            email,
            senha,
            urlImagem,
            url,
            idGrupoDoPrincipalFk,
            idGrupoDoPrincipalFacultativoFk,
            unico,
            dataCriacao,
            nome,
            titulo,
            cpf,
            cnpj,
            rg,
            celular,
            textoGrande,
            snakeCase,
            preco
    )
}

private class GrupoDoPrincipalRM(override var alias: String? = null) : RelationalMapper<GrupoDoPrincipal>() {
    override val table = "grupo_do_principal"

    val idGrupoDoPrincipalPk = col("idGrupoDoPrincipalPk",
            { idGrupoDoPrincipalPk },
            { idGrupoDoPrincipalPk = it.value() })

    val titulo = col("titulo",
            { titulo },
            { titulo = it.value() })

    fun build(rs: ResultSet) = GrupoDoPrincipal().apply {
        selectFields.forEach { col ->
            col.build(this, rs)
        }
    }

    val selectFields
        get() = arrayOf(
                idGrupoDoPrincipalPk,
                titulo
        )

    val fieldsToSearch
        get() = arrayOf(
                idGrupoDoPrincipalPk,
                titulo
        )

    val orderMap
        get() = mapOf(
                "idGrupoDoPrincipalPk" to idGrupoDoPrincipalPk,
                "titulo" to titulo
        )

    val updateSet
        get() = arrayOf(
                titulo
        )

    val insertValues
        get() = arrayOf(
                titulo
        )
}

class PrincipalListFilter {
    var query: String? = null
    var page: Int? = null
    var limit: Int? = null
    var orderBy: String? = null
    var ascending: Boolean? = null
    var idGrupoDoPrincipalFk: List<Long>? = null
    var idGrupoDoPrincipalFacultativoFk: List<Long>? = null
    var startDataObrigatoria: Date? = null
    var endDataObrigatoria: Date? = null
    var startDataFacultativa: Date? = null
    var endDataFacultativa: Date? = null
    var startDatahoraObrigatoria: Date? = null
    var endDatahoraObrigatoria: Date? = null
    var startDatahoraFacultativa: Date? = null
    var endDatahoraFacultativa: Date? = null
    var startDataCriacao: Date? = null
    var endDataCriacao: Date? = null
    var startDataAlteracao: Date? = null
    var endDataAlteracao: Date? = null
    var minDecimalObrigatorio: Double? = null
    var maxDecimalObrigatorio: Double? = null
    var minDecimalFacultativo: Double? = null
    var maxDecimalFacultativo: Double? = null
    var minInteiroObrigatorio: Long? = null
    var maxInteiroObrigatorio: Long? = null
    var minInteiroFacultativo: Long? = null
    var maxInteiroFacultativo: Long? = null
    var minPreco: Double? = null
    var maxPreco: Double? = null
    var booleanoObrigatorio: Boolean? = null
    var booleanoFacultativo: Boolean? = null
}
