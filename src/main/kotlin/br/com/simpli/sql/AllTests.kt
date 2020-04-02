package br.com.simpli.sql

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

@Ignore
class AllTests : DaoTest("jdbc/usecaseDS", "usecase") {
    val con = TransacConnector(getConnection())

    @Test
    fun resultBuilderTest() {
        val query = Query()
                .selectFields(PrincipalModel.idAndEmailFields())
                .from("principal")
                .whereSomeLikeThis(arrayOf("textoObrigatorio", "textoFacultativo"), "%lorem%")

        val result = con.getList(query) {
            PrincipalModel(ResultBuilder(PrincipalModel.idAndEmailFields(), it, "principal"))
        }

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertNotNull(result[0])
        assertNotNull(result[0].email)
    }
}

class PrincipalModel() {
    var idPrincipalPk: Long = 0
    var email: String? = null
    var url: String? = null

    constructor(rs: ResultBuilder) : this() {
        idPrincipalPk = rs.getLong("idPrincipalPk")
        email = rs.getString("email")
        url = rs.getString("url")
    }

    companion object {
        fun idAndEmailFields() = arrayOf("idPrincipalPk", "email")
    }

}
