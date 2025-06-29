package funkotlin.fp_in_kotlin_book.chapter09

import funkotlin.fp_in_kotlin_book.chapter04.Either
import java.util.regex.Pattern
import kotlin.Result as KotlinResult

typealias Parser<T> = (Location) -> Result<T>
sealed class Result<out T>
data class Success<out A>(val a: A, val consumed: Int): Result<A>()
data class Failure(val get: ParseError): Result<Nothing>()

data class ParseError(val stack: List<Pair<Location, String>>)

fun errorStack(e: ParseError): List<Pair<Location, String>> = TODO()

abstract class Parsers<PE> {
    // primitives
    internal abstract fun string(s: String): Parser<String>
    internal abstract fun regexp(r: String): Parser<String>
    internal abstract fun <A> slice(p: Parser<A>): Parser<String>
    internal abstract fun <A> succeed(a: A): Parser<A>
    internal abstract fun fail(): Parser<Nothing>
    internal abstract fun <A, B> flatMap(p1: Parser<A>, f: (A) -> Parser<B>): Parser<B>
    internal abstract fun <A> or(p1: Parser<out A>, p2: () -> Parser<out A>): Parser<A>
    
    internal abstract fun <A> tag(msg: String, pa: Parser<A>): Parser<A>
    internal abstract fun <A> scope(msg: String, pa: Parser<A>): Parser<A>
    internal abstract fun <A> attempt(pa: Parser<A>): Parser<A>

    // other combinators
    internal abstract fun char(c: Char): Parser<Char>
    internal abstract fun <A> many(pa: Parser<A>): Parser<List<A>>
    internal abstract fun <A> many1(pa: Parser<A>): Parser<List<A>>
    internal abstract fun <A> listOfN(n: Int, p: Parser<A>): Parser<List<A>>
    internal abstract fun <A, B> product(pa: Parser<A>, pb: () -> Parser<B>): Parser<Pair<A, B>>
    internal abstract fun <A, B, C> map2(pa: Parser<A>, pb: () -> Parser<B>, f: (A, B) -> C): Parser<C>
    internal abstract fun <A, B> map(pa: Parser<A>, f: (A) -> B): Parser<B>
    internal abstract fun <A> defer(pa: Parser<A>): () -> Parser<A>

    internal abstract fun <A> skipR(pa: Parser<A>, ps: Parser<String>): Parser<A>
    internal abstract fun <B> skipL(ps: Parser<String>, pb: Parser<B>): Parser<B>
    internal abstract fun <A> sep(p1: Parser<A>, p2: Parser<String>): Parser<List<A>>
    internal abstract fun <A> surround(start: Parser<String>, stop: Parser<String>, p: Parser<A>): Parser<A>

    // TODO move
    abstract fun <A> run(p: Parser<A>, input: String): Either<PE, A>
}

abstract class ParsersDsl<PE> : Parsers<PE>() {
    // syntactic sugar
    fun <A> Parser<A>.defer(): () -> Parser<A> = defer(this)

    fun <A, B> Parser<A>.map(f: (A) -> B): Parser<B> = this@ParsersDsl.map(this, f)

    fun <A> Parser<A>.many(): Parser<List<A>> = this@ParsersDsl.many(this)

    infix fun <A> Parser<out A>.or(p: Parser<out A>): Parser<A> =
        this@ParsersDsl.or(this, p.defer())

    infix fun String.or(other: String): Parser<String> =
        this@ParsersDsl.or(string(this), string(other).defer())

    fun <A> tag(msg: String, pa: () -> Parser<A>): Parser<A> = tag(msg, pa())
    fun <A> scope(msg: String, pa: () -> Parser<A>): Parser<A> = scope(msg, pa())

    infix fun <A, B> Parser<A>.product(p: Parser<B>): Parser<Pair<A, B>> =
        this@ParsersDsl.product(this, p.defer())

    infix fun <A> Parser<A>.sep(ps: Parser<String>): Parser<List<A>> =
        this@ParsersDsl.sep(this, ps)

    infix fun <A> Parser<A>.skipR(ps: Parser<String>): Parser<A> =
        this@ParsersDsl.skipR(this, ps)

    infix fun <B> Parser<String>.skipL(pb: Parser<B>): Parser<B> =
        this@ParsersDsl.skipL(this, pb)

    infix fun <T> T.cons(la: List<T>) = listOf(this) + la
}


sealed class JSON {
    object JNull : JSON()
    data class JNumber(val get: Double) : JSON()
    data class JString(val get: String) : JSON()
    data class JBoolean(val get: Boolean) : JSON()
    data class JArray(val get: List<JSON>) : JSON()
    data class JObject(val get: Map<String, JSON>) : JSON()
}

data class Location(val input: String, val offset: Int = 0) {
    private val slice by lazy { input.slice(0..offset + 1) }

    val line by lazy { slice.count { it == '\n' } + 1 }
    val column by lazy {
        when (val n = slice.lastIndexOf('\n')) {
            -1 -> offset + 1
            else -> offset - n
        }
    }
}

abstract class JsonParsers : ParsersDsl<ParseError>() {
    val JSON.parser: Parser<JSON>
        get() = succeed(this)

    val String.rp: Parser<String>
        get() = regexp(this)
    val String.sp: Parser<String>
        get() = string(this)

    fun thru(s: String): Parser<String> =
        ".*?${Pattern.quote(s)}".rp
    val quoted: Parser<String> =
        "\"".sp skipL thru("\"").map { it.dropLast(1) }

    val doubleString: Parser<String> =
        "[-+]?([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?".rp
    val double: Parser<Double> = doubleString.map { it.toDouble() }

    val lit: Parser<JSON> =
        JSON.JNull.parser or
                double.map { JSON.JNumber(it) } or
                JSON.JBoolean(true).parser or
                JSON.JBoolean(false).parser or
                quoted.map { JSON.JString(it) }

    val value: Parser<JSON> = lit or obj() or array()
    val keyval: Parser<Pair<String, JSON>> = quoted product (":".sp skipL value)

    val whitespace: Parser<String> = """\s*""".rp
    val eof: Parser<String> = """\z""".rp

    fun array(): Parser<JSON.JArray> =
        surround("[".sp, "]".sp,
            (value sep ",".sp).map { vs -> JSON.JArray(vs) }
        )

    fun obj(): Parser<JSON.JObject> =
        surround("{".sp, "}".sp,
            (keyval sep ",".sp).map { kvs -> JSON.JObject(kvs.toMap()) })

    fun <A> root(p: Parser<A>): Parser<A> = p skipR eof

    val jsonParser: Parser<out JSON> =
        root(whitespace skipL (obj() or array()))
}

object ParsersInterpreter : ParsersDsl<ParseError>() {
    override fun string(s: String): Parser<String> = { loc: Location ->
        if (loc.input.startsWith(s))
            Success(s, s.length)
        else
            Failure(loc.toError("Expected: $s"))
    }

    override fun regexp(r: String): Parser<String> =
        TODO("Not yet implemented")

    override fun <A> slice(p: Parser<A>): Parser<String> =
        TODO("Not yet implemented")

    override fun <A> succeed(a: A): Parser<A> =
        string("").map { a }

    override fun fail(): Parser<Nothing> {
        TODO("Not yet implemented")
    }

    override fun <A, B> flatMap(
        p1: Parser<A>,
        f: (A) -> Parser<B>,
    ): Parser<B> =
        TODO("Not yet implemented")
    override fun <A> or(
        p1: Parser<out A>,
        p2: () -> Parser<out A>,
    ): Parser<A> =
        TODO("Not yet implemented")

    override fun <A> tag(
        msg: String,
        pa: Parser<A>,
    ): Parser<A> =
        TODO("Not yet implemented")

    override fun <A> scope(
        msg: String,
        pa: Parser<A>,
    ): Parser<A> =
        TODO("Not yet implemented")

    override fun <A> attempt(pa: Parser<A>): Parser<A> {
        TODO("Not yet implemented")
    }

    override fun char(c: Char): Parser<Char> =
        string(c.toString()).map { it[0] }

    override fun <A> many(pa: Parser<A>): Parser<List<A>> =
        or(
            map2(pa, defer(many(pa))) { a, la -> a cons la },
            defer(succeed(emptyList<A>()))
        )

    override fun <A> many1(pa: Parser<A>): Parser<List<A>> =
        map2(pa, many(pa).defer()) { a: A, la: List<A> -> la }

    override fun <A> listOfN(
        n: Int,
        pa: Parser<A>
    ): Parser<List<A>> =
        if (n > 0) {
            map2(pa, listOfN(n - 1, pa).defer()) { a, la -> listOf(a) + la }
        } else {
            succeed(emptyList())
        }

    override fun <A, B> product(
        pa: Parser<A>,
        pb: () -> Parser<B>,
    ): Parser<Pair<A, B>> =
        flatMap(pa) { a -> map(pb()) { b -> Pair(a, b) } }

    override fun <A, B, C> map2(
        pa: Parser<A>,
        pb: () -> Parser<B>,
        f: (A, B) -> C,
    ): Parser<C> =
        flatMap(pa) { a -> map(pb()) { b -> f(a, b) } }

    override fun <A, B> map(
        pa: Parser<A>,
        f: (A) -> B,
    ): Parser<B> =
        flatMap(pa) { a -> succeed(f(a)) }

    override fun <A> defer(pa: Parser<A>): () -> Parser<A> =
        { pa }

    override fun <A> skipR(
        pa: Parser<A>,
        ps: Parser<String>,
    ): Parser<A> =
        TODO("Not yet implemented")

    override fun <B> skipL(
        ps: Parser<String>,
        pb: Parser<B>,
    ): Parser<B> =
        TODO("Not yet implemented")

    override fun <A> sep(
        p1: Parser<A>,
        p2: Parser<String>,
    ): Parser<List<A>> =
        TODO("Not yet implemented")

    override fun <A> surround(
        start: Parser<String>,
        stop: Parser<String>,
        p: Parser<A>,
    ): Parser<A> =
        TODO("Not yet implemented")

    override fun <A> run(
        p: Parser<A>,
        input: String,
    ): Either<ParseError, A> =
        TODO()
}

private fun Location.toError(msg: String) =
    ParseError(listOf(this to msg))
