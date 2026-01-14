/**
 * Optional typed views around generated UCP models.
 *
 * <p>The UCP schema intentionally leaves some fields open-ended (e.g. {@code List<Object>} or
 * {@code Map<String, Object>}) to allow extensions and forward-compatibility. The classes in this
 * package provide conveniences to read/write a few commonly used shapes without changing the
 * underlying generated types.
 *
 * <p>These helpers are additive: you can ignore them and interact with the generated model fields
 * directly.
 */
package io.deeplumen.ucp.typed;

