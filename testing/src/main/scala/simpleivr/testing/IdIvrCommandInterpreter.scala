package simpleivr.testing

import cats.Id
import simpleivr.IvrCommand


class IdIvrCommandInterpreter extends IvrCommand.Interpreter[Id]
