package dev.wilding.lox;

import lombok.Getter;

class Return extends RuntimeException {
  @Getter final Object value;

  Return(Object value) {
    super(null, null, false, false);
    this.value = value;
  }
}
