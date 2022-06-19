# 1. thread 创建的3个方式

## 1.1 thread 函数

```c++
#include<thread>
void th_work(int args);
...
std::thread th1(th_work,args);
th1.join()//th1.detach()
```

## 1.2 lambda

```c++
std:: thread([](){
    //函数执行内容
}).join();

std:: thread th2([](){
    //函数体
});
th2.detach();
```

## 1.3 可调用类对象

```c++
class tmp
{
  public:
    void operator()()
    {
    //函数体
    }  
};
...
tmp tmp_instance1;
std::thread th3(tmp_instance1);
th3.join();
```

子线程 调用join()，会阻塞在被调用处，直到子线程执行完；调用detach()，被调用处不会等待子线程的执行，此时子线程会被系统接管。  detach时如果子线程用到了外部传进来的参数，可能会访问到不可预料的结果（无法保证外部参数在子线程执行时一直有效，）

## 1.4 thread 参数传递

**普通简单类型**，值传递，detach无影响

**指针**，地址相同，detach时，指针指向的外部变量如果变化子线程会不可预料

**类对象**，分3种

```c++
void th3_work1(const tmp& tmp1);
tmp tmp1;
1. 类对象
std::thread th3(th3_work1,tmp1);//虽然是接收类的引用参数，但传递的还是拷贝的对象

2. 类对象的引用       
std::thread th3(th3_work1,std::ref(tmp1);//真正的引用传递，
//子线程无法管理对象的生命周期但又会使用它，detach时就会访问已析构的对象

3.临时对象 
std::thread th3(th3_work1,tmp2);    
//隐式转换时发生在子线程
std::thread th3(th3_work1,(tmp)tmp2);  
// 在主线程 显示转换后，传参就变成第一个，会进行拷贝
```

# 2. mutex 锁

共享资源需确保同一时刻只有一个线程去操作，否则“一张票会卖给多个人”

```c++
#include<mutex>

1. lock/unlock
mutex mtx;
mtx.lock();
....
mtx.unlock();

2. lock_guard
声明一个局部的std::lock_guard对象，在其构造函数中进行加锁，在其析构函数中进行解锁。
最终的结果就是：创建即加锁，作用域结束自动解锁
{
    lock_guard<mutex> lg1(mtx);
    ....
}
3. unique_lock
同lock_guard,功能更丰富
```

# 3. 条件变量,配合mutex同步线程

```c++
#include<condition_variable>
#include<mutex>

std::mutex mtx;
std::condition_variable cond;


{//func1
    std::unique_lock<mutex> ulk(mtx);//必须unique_lock
    。。。
    cond.notify_one()//func1业务执行完毕，通知一个等待中的func2
}

{//func2
   while(){  //wait操作多半处于while循环中不能放在if中
    std::unique_lock<mutex> ulk(mtx);
    .....
    cond.wait(ulk);//解锁互斥量并休眠等待被唤醒。唤醒后加锁处理后面语句的业务

    .....//    唤醒后处理的业务逻辑
    }
} 

cond.wait(ulk,[](){return true/flase;}); 
//第二个参数 为真 不等待直接返回，为假会阻塞等待被唤醒。
```

# 4. 线程池




