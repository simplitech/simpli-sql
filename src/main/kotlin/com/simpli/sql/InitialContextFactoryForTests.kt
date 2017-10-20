package com.simpli.sql

import java.util.*
import javax.naming.Context
import javax.naming.InitialContext
import javax.naming.NamingException
import javax.naming.spi.InitialContextFactory


class InitialContextFactoryForTests : InitialContextFactory {

    @Throws(NamingException::class)
    override fun getInitialContext(environment: Hashtable<*, *>): Context? {
        return context
    }

    fun getInitialContext(): Context? {
        return context
    }

    companion object {

        private var context: Context? = null

        init {
            try {
                context = object : InitialContext(true) {
                    internal var bindings: MutableMap<String, Any> = HashMap()

                    @Throws(NamingException::class)
                    override fun bind(name: String, obj: Any) {
                        bindings.put(name, obj)
                    }

                    @Throws(NamingException::class)
                    override fun lookup(name: String): Any? {
                        return bindings[name]
                    }
                }
            } catch (e: NamingException) { // can't happen.
                throw RuntimeException(e)
            }

        }

        fun bind(name: String, obj: Any) {
            try {
                context!!.bind(name, obj)
            } catch (e: Exception) { // can't happen.
                throw RuntimeException(e)
            }

        }
    }

}