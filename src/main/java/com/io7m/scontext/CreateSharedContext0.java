/*
 * Copyright © 2015 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.scontext;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class CreateSharedContext0
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(CreateSharedContext0.class);
  }

  private CreateSharedContext0()
  {
    throw new AssertionError("Unreachable code!");
  }

  public static void main(
    final String[] args)
  {
    CreateSharedContext0.LOG.debug("fetching GL3 profile");

    final GLProfile pro = GLProfile.get(GLProfile.GL3);
    final GLCapabilities caps = new GLCapabilities(pro);
    caps.setFBO(true);

    CreateSharedContext0.LOG.debug("creating offscreen drawable factory");
    final GLDrawableFactory draw_fact = GLDrawableFactory.getFactory(pro);

    CreateSharedContext0.LOG.debug("creating master context");
    final GLOffscreenAutoDrawable master =
      draw_fact.createOffscreenAutoDrawable(null, caps, null, 640, 480);
    master.display();
    final GLContext master_ctx =
      Objects.requireNonNull(master.getContext(), "Master context");

    CreateSharedContext0.LOG.debug("creating slave context");
    final GLOffscreenAutoDrawable slave =
      draw_fact.createOffscreenAutoDrawable(null, caps, null, 640, 480);
    slave.setSharedAutoDrawable(master);
    slave.display();
    final GLContext slave_ctx = Objects.requireNonNull(
      slave.getContext(), "Slave context");

    CreateSharedContext0.LOG.debug(
      "slave is shared with master: {}",
      slave_ctx.getCreatedShares().contains(master_ctx));
    CreateSharedContext0.LOG.debug(
      "master is shared with slave: {}",
      master_ctx.getCreatedShares().contains(slave_ctx));

    CreateSharedContext0.LOG.debug("destroying contexts");
    master.destroy();
    slave.destroy();
  }
}
