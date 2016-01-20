## Demos

### `Option`

```
$ sbt "run-main OptionDemo 6 7"
parseAndMultiply("6", "7") = Some(42)
```

```
$ sbt "run-main OptionDemo six 7"
parseAndMultiply("six", "7") = None
```

```
$ sbt "run-main OptionDemo 6 several"
parseAndMultiply("6", "several") = None
```

### `Validation`

```
$ sbt "run-main ValidationDemo 6 7"
parseAndMultiply("6", "7") = Success(42)
```

```
$ sbt "run-main ValidationDemo six 7"
parseAndMultiply("six", "7") = Failure([could not parse "six" as an integer])
```

```
$ sbt "run-main ValidationDemo six seven"
parseAndMultiply("six", "seven") = Failure([could not parse "six" as an integer, could not parse "seven" as an integer])
```

### `Reader`

```
$ sbt "run-main ReaderDemo"
Balance: $0.00
Balance: $6.00
Balance: $42.00
```
