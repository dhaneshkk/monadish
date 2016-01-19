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

### `Reader`

```
$ sbt "run-main ReaderDemo"
Balance: $0.00
Balance: $6.00
Balance: $42.00
```
