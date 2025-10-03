package funkotlin.fp_in_kotlin_book.chapter09

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.lastOrNone
import arrow.core.toOption
import arrow.Kind

import funkotlin.fp_in_kotlin_book.chapter04.Either
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter05.Stream
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.cons
import java.util.regex.Pattern

class ForParser private constructor() { companion object }
typealias ParserOf<T> = Kind<ForParser, T>
fun <A> ParserOf<A>.fix() = this as Parser<A>

data class Parser<T>(val run: (Location) -> Result<T>) : ParserOf<T>

typealias State = Location

sealed class Result<out T>
data class Success<out A>(
    val a: A,
    val consumed: Int
): Result<A>()
data class Failure(
    val get: ParseError,
    val isCommited: Boolean
): Result<Nothing>()

fun <A> Result<A>.uncommit(): Result<A> =
    when(this) {
        is Failure -> if (this.isCommited) Failure(this.get, false) else this
        is Success -> this
    }

fun <A> Result<A>.addCommit(commit: Boolean): Result<A> =
    when(this) {
        is Failure -> Failure(get = this.get, isCommited = this.isCommited || commit)
        is Success -> this
    }

fun <A> Result<A>.advanceSuccess(n: Int): Result<A> =
    when(this) {
        is Success -> Success(this.a, this.consumed + n)
        is Failure -> this
    }

fun <A> Result<A>.mapError(f: (ParseError) -> ParseError): Result<A> =
    when(this) {
        is Success -> this
        is Failure -> Failure(f(this.get), this.isCommited)
    }

data class ParseError(val stack: List<Pair<Location, String>> = emptyList()) {
    fun push(loc: Location, msg: String): ParseError =
        this.copy(stack = (loc to msg) cons this.stack)

    fun label(s: String): ParseError =
        ParseError(latestLoc()
            .map { it to s}
            .toList()
        )

    private fun latestLoc(): Option<Location> = latest().map { it.first }

    private fun latest(): Option<Pair<Location, String>> = stack.lastOrNone()

    /** Display collapsed error stack - any adjustment stack elements
     * with the same location are combined on one line.
     * For the bottommost error we display the full line,
     * with a caret pointing to the column of the error.
     * Example:
     * 1.1. file 'companies.json'; array
     * 5.1 object
     * 5.2 key-value
     * 5.10 ':'
     * { "MSFT" ; 24,
     *          ^
     *
     */
    override fun toString(): String =
        if (stack.isEmpty()) "no errors" else {
            val collapsed = collapseStack(stack)
            val context = collapsed.lastOrNone()
                .map { "\n\n" + it.first.line }
                .getOrElse { "" } +
                    collapsed.lastOrNone()
                        .map { it: Pair<Location, String> -> "\n" + it.first.column }
                        .getOrElse { "" }

            collapsed.joinToString { (loc, msg) ->
                "${loc.line}.${loc.column} $msg"
            } + context
        }

    private fun collapseStack(stack: List<Pair<Location, String>>): List<Pair<Location, String>> =
        stack.groupBy { it.first }
            .mapValues { e -> e.value.map { it.second } }
            .mapValues { it.value.joinToString("; ") }
            .toList()
            .sortedBy { it.first.offset }
}

fun ParseError.tag(msg: String): ParseError {
    val latest = this.stack.lastOrNone()
    val latestLocation = latest.map { it.first }
    return ParseError(latestLocation.map { it to msg }.toList())
}

fun errorStack(e: ParseError): List<Pair<Location, String>> = e.stack

abstract class Parsers<PE> {
    // primitives
    internal abstract fun string(s: String): Parser<String>
    internal abstract fun regexp(r: String): Parser<String>
    internal abstract fun <A> slice(p: Parser<A>): Parser<String>
    internal abstract fun <A> succeed(a: A): Parser<A>
    internal abstract fun fail(): Parser<Nothing>
    abstract fun <A, B> flatMap(p1: Parser<A>, f: (A) -> Parser<B>): Parser<B>
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
    abstract fun <A, B, C> map2(pa: Parser<A>, pb: () -> Parser<B>, f: (A, B) -> C): Parser<C>
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
    override fun string(s: String): Parser<String> = Parser { loc: Location ->
        if (loc.input.startsWith(s))
            Success(s, s.length)
        else
            Failure(get = loc.toError("Expected: $s"), isCommited = true)
    }

    override fun regexp(r: String): Parser<String> = Parser { state ->
        when (val prefix = state.input.findPrefixOf(r.toRegex())) {
            is Some -> Success(prefix.t.value, prefix.t.value.length)
            is None -> Failure(get = state.toError("regex ${r.toRegex()}"), isCommited = true)
        }
    }

    private fun String.findPrefixOf(r: Regex): Option<MatchResult> =
        r.find(this).toOption().filter { it.range.first == 0}


    override fun <A> slice(p: Parser<A>): Parser<String> = Parser { state ->
        when (val result = p.run(state)) {
            is Success -> Success(state.slice(result.consumed), result.consumed)
            is Failure -> result
        }
    }

    override fun <A> succeed(a: A): Parser<A> =
        Parser { state -> Success(a, 0) }

    override fun fail(): Parser<Nothing> =
        Parser { state -> Failure(get = ParseError(listOf(state to "FAIL")), isCommited = true) }

    override fun <A, B> flatMap(
        p1: Parser<A>,
        f: (A) -> Parser<B>,
    ): Parser<B> = Parser { state ->
        when (val result = p1.run(state)) {
            is Success ->
                f(result.a).run(state.advanceBy(result.consumed))
                    .addCommit(result.consumed != 0)
                    .advanceSuccess(result.consumed)
            is Failure -> result
        }
    }

    override fun <A> or(
        p1: Parser<out A>,
        p2: () -> Parser<out A>,
    ): Parser<A> = Parser { state ->
        when (val r: Result<A> = p1.run(state) ) {
            is Failure -> if (!r.isCommited) p2().run(state) else r
            is Success -> r
        }
    }

    override fun <A> tag(
        msg: String,
        pa: Parser<A>,
    ): Parser<A> = Parser { state ->
        pa.run(state).mapError { pe: ParseError ->
            pe.tag(msg)
        }
    }

    override fun <A> scope(
        msg: String,
        pa: Parser<A>,
    ): Parser<A> = Parser { state ->
        pa.run(state).mapError { pe -> pe.push(state, msg)}
    }

    override fun <A> attempt(pa: Parser<A>): Parser<A> = Parser { s -> pa.run(s).uncommit() }

    override fun char(c: Char): Parser<Char> =
        string(c.toString()).map { it[0] }

    //        map2(pa, defer(many(pa))) { a, la ->
    override fun <A> many(pa: Parser<A>): Parser<List<A>> =
        map2(pa, { -> many(pa) }) { a, la ->
            a cons la
        } or succeed(emptyList<A>())

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

    override fun <A> defer(pa: Parser<A>): () -> Parser<A> = { pa }

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
        when (val res = p.run(Location(input))) {
            is Success -> Right(res.a)
            is Failure -> Left(res.get)
        }
}

private fun State.slice(n: Int) =
    this.input.substring(this.offset..this.offset + n)

private fun Location.toError(msg: String) =
    ParseError(listOf(this to msg))

private fun Location.advanceBy(n: Int) =
    this.copy(offset = this.offset + n)
