function helloWorld(){
    console.log("[0] Hello World");
    console.log("[1] Hello" + "World");
    var hello = "[2]Hello";
    var world = "[3]World";
    console.log("[4]"+hello);
    console.log("[5]"+world);
    console.log("[6]"+hello+world);
    console.log("[7]"+hello());
    console.log("[8]"+world());
    // console.log(hello()+world());

    console.log("[9]"+concat(hello, world));

    var test = 1;
    var test = 2;
    console.log("[10]"+test);


    fib(1, 1);
}

function hello(){
    return world();
}

function world() {
    return "[5]World";
}

function concat(par1, par2){
    console.log(par1 + par2);
    return "Nothing";
}

function fib(par1, par2){
    console.log("1:"+par1);
    console.log("2:"+par2);
    var test = (par1 + par2);
    console.log(test);
    return fib(par2, test);
}