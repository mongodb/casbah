
/** Load this table from the yield_historical_in.json file */

db.yield_historical.out.aughts.drop();
db.yield_historical.out.aughts.reduced.drop();

print("Setup run");

function m() { 
    key = typeof( this._id ) == "number" ? this._id : this._id.getYear(); 
    emit( key, { count: 1, sum: this.bc10Year } ) ;
}

function r( year, values ) { 
  var n = { count: 0, sum: 0 } 
  for ( var i = 0; i < values.length; i++ ){ 
      n.sum += values[i].sum; 
      n.count += values[i].count; 
  } 
   
  return n; 
} 

function f( year, value ){
  value.avg = value.sum / value.count;
  return value;
}

res = db.runCommand(
  { 
    "mapreduce": "yield_historical.in",
    "map": m,
    "reduce": r,
    "finalize": f,
    "verbose" : true , 
    "out" : { "inline" : 1 }
  }
)

res = db.runCommand(
  { 
    "mapreduce": "yield_historical.in",
    "map": m,
    "reduce": r,
    "finalize": f,
    "query" : { "_id" : { "$lt" : new Date(2000, 0, 1) } },
    "verbose" : true , 
    "out" : "yield_historical.merged",
  }
)

res = db.runCommand(
  { 
    "mapreduce": "yield_historical.in",
    "map": m,
    "reduce": r,
    "finalize": f,
    "query" : { "_id" : { "$gt" : new Date(2000, 0, 1) } },
    "verbose" : true , 
    "out" : { "merge" : "yield_historical.merged" },
  }
)

assert.eq( res.ok, 1.0 );

print("Merging worked.")

res = db.yield_historical.in.mapReduce( m, r, 
  { 
    "query" : { "_id" : { "$gt" : new Date(2000, 0, 1) } },
    "verbose" : true , 
    "out" : { "reduce" : "yield_historical.out.aughts" },
  }
)

assert.eq( res.ok, 1.0 );

print("Reducing into existing collection worked.")

res = db.yield_historical.in.mapReduce( m, r, 
  { 
    "query" : { "_id" : { "$gt" : new Date(2000, 0, 1) } },
    "verbose" : true , 
    "out" : { "reduce" : "yield_historical.out.aughts.reduced" },
  }
)

assert.eq( res.ok, 1.0 );

res = db.runCommand(
  { 
    "mapreduce": "yield_historical.in",
    "map": m,
    "reduce": r,
    "finalize": f,
    "query" : { "_id" : { 
        "$gte": new Date(2001, 0, 1),
        "$lte" : new Date(2001, 5, 1) 
    } },
    "verbose" : true , 
    "out" : "yield_historical.reduced",
  }
)

res = db.runCommand(
  { 
    "mapreduce": "yield_historical.in",
    "map": m,
    "reduce": r,
    "finalize": f,
    "query" : { "_id" : { 
        "$gt": new Date(2001, 5, 1),
        "$lte" : new Date(2001, 11, 31) 
    } },
    "verbose" : true , 
    "out" : { "reduce" : "yield_historical.reduced" },
  }
)
