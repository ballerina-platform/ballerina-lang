import ballerina/runtime;

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

type TeacherOutput record {
    string teacherName;
    int age;
};

int index = 0;
stream<Teacher> inputStream = new;
stream<TeacherOutput> outputStream = new;
TeacherOutput[] globalEmployeeArray = [];

type Stock record {
    string symbol;
    float price;
    int volume;
};

type Twitter record {
    string user;
    string tweet;
    string company;
};

type StockWithPrice record {
    string symbol;
    string tweet;
    float price;
};

stream<Stock> stockStream = new;
stream<Twitter> twitterStream = new;
stream<StockWithPrice> stockWithPriceStream = new;

function testJoinQuery() {

    forever {
        from stockStream window length(1)
        join twitterStream window length(1)
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }
        from stockStream window length(1)
        join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }
        from stockStream window length(1)
        unidirectional join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        from stockStream window length(1)
        join unidirectional twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }
        from stockStream window length(1)
        left outer join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        from stockStream window length(1)
        right outer join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        from stockStream window length(1)
        full outer join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        from stockStream window length(1)
        outer join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        from stockStream window length(1)
        inner join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        // Unidirectional added first
        from stockStream window length(1)
        unidirectional left outer join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }
        from stockStream window length(1)
        unidirectional right outer join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }
        from stockStream window length(1)
        unidirectional full outer join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }
        from stockStream window length(1)
        unidirectional outer join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }
        from stockStream window length(1)
        unidirectional inner join twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        // Unidirectional added after join type.
        from stockStream window length(1)
        left outer join unidirectional twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        from stockStream window length(1)
        right outer join unidirectional twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        from stockStream window length(1)
        full outer join unidirectional twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        from stockStream window length(1)
        outer join unidirectional twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }

        from stockStream window length(1)
        inner join unidirectional twitterStream window length(1)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            foreach var e in emp {
                stockWithPriceStream.publish(e);
            }
        }
    }

}

function testSelectQuery() {
    forever {
        from inputStream
        select inputStream.name as teacherName, inputStream.age
        => (TeacherOutput[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
            }
        }

        from inputStream where inputStream.age > 25
        select inputStream.name, inputStream.age, count() as count
        group by getGroupByField(inputStream.age)
        => (TeacherOutput[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
            }
        }

        from inputStream
        select inputStream.name, inputStream.age
        having age > getMaxAge() && age > 25
        => (TeacherOutput[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
            }
        }

        from inputStream where inputStream.age > 25 window length(5)
        select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
        group by inputStream.name
        => (TeacherOutput[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
            }
        }
    }

}

function orderBy() {
    forever {
        from inputStream window lengthBatch(10)
        select inputStream.name, inputStream.age
        order by inputStream.age
        => (TeacherOutput[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
            }
        }

        from inputStream window lengthBatch(10)
        select inputStream.name, inputStream.age
        order by inputStream.age ascending
        => (TeacherOutput[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
            }
        }

        from inputStream window lengthBatch(10)
        select inputStream.name, inputStream.age
        order by inputStream.age ascending, inputStream.name
        => (TeacherOutput[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
            }
        }

        from inputStream window lengthBatch(10)
        select inputStream.name, inputStream.age
        order by inputStream.age, inputStream.name descending
        => (TeacherOutput[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
            }
        }

        from inputStream window lengthBatch(10)
        select inputStream.name, inputStream.age
        order by inputStream.age ascending, inputStream.name descending
        => (TeacherOutput[] teachers) {
            foreach var t in teachers {
                outputStream.publish(t);
            }
        }
    }
}

function outputRate() {
    forever {
        from teacherStream9
        select name, age, status
        output first every 3 seconds
        => (Employee[] emp) {
            foreach var e in emp {
                employeeStream8.publish(e);
            }
        }

        from teacherStream9
        select name, age, status
        output first every 3 events
        => (Employee[] emp) {
            foreach var e in emp {
                employeeStream8.publish(e);
            }
        }

        from teacherStream9
        select name, age, status
        output snapshot every 3 events
        => (Employee[] emp) {
            foreach var e in emp {
                employeeStream8.publish(e);
            }
        }
    }
}
