package org.testeditor.fixture.core;

import org.apache.logging.log4j.message.FormattedMessage;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class DefaultMaskingLogger implements Logger {

	Logger delegate;
	StringMasker masker;

	public DefaultMaskingLogger(Logger logger, StringMasker masker) {
		this.delegate = logger;
		this.masker = masker;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public boolean isTraceEnabled() {
		// TODO Auto-generated method stub
		return delegate.isTraceEnabled();
	}

	@Override
	public void trace(String msg) {
		delegate.trace(masker.mask(msg));
	}

	@Override
	public void trace(String format, Object arg) {
		delegate.trace(masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		delegate.trace(masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void trace(String format, Object... arguments) {
		delegate.trace(masker.mask(new FormattedMessage(format, arguments).getFormattedMessage()));
	}

	@Override
	public void trace(String msg, Throwable t) {
		delegate.trace(masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return delegate.isTraceEnabled(marker);
	}

	@Override
	public void trace(Marker marker, String msg) {
		delegate.trace(marker, masker.mask(msg));
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		delegate.trace(marker, masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		delegate.trace(marker, masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {
		delegate.trace(marker, masker.mask(new FormattedMessage(format, argArray).getFormattedMessage()));
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		delegate.trace(marker, masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

	@Override
	public boolean isDebugEnabled() {
		return delegate.isDebugEnabled();
	}

	@Override
	public void debug(String msg) {
		delegate.debug(masker.mask(msg));
	}

	@Override
	public void debug(String format, Object arg) {
		delegate.debug(masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		delegate.debug(masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void debug(String format, Object... arguments) {
		delegate.debug(masker.mask(new FormattedMessage(format, arguments).getFormattedMessage()));
	}

	@Override
	public void debug(String msg, Throwable t) {
		delegate.debug(masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return delegate.isDebugEnabled(marker);

	}

	@Override
	public void debug(Marker marker, String msg) {
		delegate.debug(marker, masker.mask(msg));
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		delegate.debug(marker, masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));

	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		delegate.debug(marker, masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {
		delegate.debug(marker, masker.mask(new FormattedMessage(format, arguments).getFormattedMessage()));
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		delegate.debug(marker, masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

	@Override
	public boolean isInfoEnabled() {
		return delegate.isInfoEnabled();
	}

	@Override
	public void info(String msg) {
		delegate.info(masker.mask(msg));
	}

	@Override
	public void info(String format, Object arg) {
		delegate.info(masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		delegate.info(masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void info(String format, Object... arguments) {
		delegate.info(masker.mask(new FormattedMessage(format, arguments).getFormattedMessage()));
	}

	@Override
	public void info(String msg, Throwable t) {
		delegate.info(masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return delegate.isInfoEnabled(marker);
	}

	@Override
	public void info(Marker marker, String msg) {
		delegate.info(marker, masker.mask(msg));
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		delegate.info(marker, masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		delegate.info(marker, masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void info(Marker marker, String format, Object... arguments) {
		delegate.info(marker, masker.mask(new FormattedMessage(format, arguments).getFormattedMessage()));
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		delegate.info(marker, masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

	@Override
	public boolean isWarnEnabled() {
		return delegate.isWarnEnabled();
	}

	@Override
	public void warn(String msg) {
		delegate.warn(masker.mask(msg));
	}

	@Override
	public void warn(String format, Object arg) {
		delegate.warn(masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));
	}

	@Override
	public void warn(String format, Object... arguments) {
		delegate.warn(masker.mask(new FormattedMessage(format, arguments).getFormattedMessage()));
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		delegate.warn(masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void warn(String msg, Throwable t) {
		delegate.warn(masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return delegate.isWarnEnabled(marker);
	}

	@Override
	public void warn(Marker marker, String msg) {
		delegate.warn(marker, masker.mask(msg));
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		delegate.warn(marker, masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		delegate.warn(marker, masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {
		delegate.warn(marker, masker.mask(new FormattedMessage(format, arguments).getFormattedMessage()));
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		delegate.warn(marker, masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

	@Override
	public boolean isErrorEnabled() {
		return delegate.isErrorEnabled();
	}

	@Override
	public void error(String msg) {
		delegate.error(masker.mask(msg));
	}

	@Override
	public void error(String format, Object arg) {
		delegate.error(masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		delegate.error(masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void error(String format, Object... arguments) {
		delegate.error(masker.mask(new FormattedMessage(format, arguments).getFormattedMessage()));
	}

	@Override
	public void error(String msg, Throwable t) {
		delegate.error(masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return delegate.isErrorEnabled(marker);
	}

	@Override
	public void error(Marker marker, String msg) {
		delegate.error(marker, masker.mask(msg));
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		delegate.error(marker, masker.mask(new FormattedMessage(format, arg).getFormattedMessage()));
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		delegate.error(marker, masker.mask(new FormattedMessage(format, arg1, arg2).getFormattedMessage()));
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {
		delegate.error(marker, masker.mask(new FormattedMessage(format, arguments).getFormattedMessage()));
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		delegate.error(marker, masker.mask(new FormattedMessage(msg, null, t).getFormattedMessage()));
	}

}
