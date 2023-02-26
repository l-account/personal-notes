# #1. 智能指针

## 1.1 std::unique_ptr<>

独占资源所有权的指针，离开 unique_ptr 对象的作用域时，会自动释放资源

```cpp
{
    std::unique_ptr<int> uptr = std::make_unique<int>(200);
    //...
    // 离开 uptr 的作用域的时候自动释放内存
}


{
    std::unique_ptr<int> uptr = std::make_unique<int>(200);
    std::unique_ptr<int> uptr1 = uptr;  // 编译错误，std::unique_ptr<T> 是 move-only 的

    std::unique_ptr<int> uptr2 = std::move(uptr);
    assert(uptr == nullptr);
}


std::unique_ptr<int[]> uptr = std::make_unique<int[]>(10); //指向数组 
uptr[i] = i * i;

//自定义deleter，unique_ptr<T,D> u,unique_ptr<T,D> u(d)
{
    std::unique_ptr<FILE, std::function<void(FILE*)>> uptr(
        fopen("test_file.txt", "w"), [](FILE* fp) {
            fclose(fp);
        });
}
{
    struct FileCloser {
        void operator()(FILE* fp) const {
            if (fp != nullptr) {
                fclose(fp);
            }
        }   
    };  
    std::unique_ptr<FILE, FileCloser> uptr(fopen("test_file.txt", "w"));
}
```

## 1.2 std::shared_ptr<> ，std::weak_ptr<>

 shared_ptr:共享资源所有权的指针,引用计数为0后自定释放资源，

weak_ptr:共享资源的观察者，需要和 shared_ptr 一起使用，不影响资源的生命周期

    不改变资源的引用计数、持有的计数是同一资源观察者的计数、无法直接访问资源即解引用操作，需通过lock()方法提升为shared_ptr

`shared_ptr` 有两个数据成员，一个是指向 对象的指针 `ptr`，另一个是 `ref_count` 指针指向控制信息 （包含vptr、use_count、weak_count、ptr等）；

```cpp
std::shared_ptr<int> sp_ptr;
std::weak_ptr<int> wp_ptr = sp_ptr;
wp_ptr.use_count()  //观测资源的引用计数
wp_ptr.expired()    //被观测的资源是否销毁、可用
std::shared_ptr<int> sp2 = wp_ptr.lock()  
//expired()true时获得一个可用shared_ptr对象，否则返回null
```

# 2.  std::bind,std::function

std::function<返回值（参数类型列表）>, 函数打包，普通函数、类对象仿函数、lambda

```cpp
#include<functional>
void f1(int a,int b,int c){
    std::cout<<a<<b<<c<<std::endl;
}
std::function<void(int,int,int)> task= 
std::bind(f1,std::placeholders::_2,std::placeholders::_3,std::placeholders::_1);
f1(1,2,3);// 1,2,3
task(1,2,3)//3,1,2,   第一个参数是1，bind时申明占f1函数的第二个参数位置。。。
```

# 3. move、forward

```cpp
/*
* 临时变量copy 开销很大
* 1. rvalue reference传临时变量, move语义避免copy
* 2. (优化)forward同时能处理rvalue/lvalue reference和const reference
*/

void set(string && var1, string && var2){
  //avoid unnecessary copy!
  m_var1 = std::move(var1);  
  m_var2 = std::move(var2);
}
A a1;
//temporary, move! no copy! 成员变量的地址指向右值地址，避免先拷贝右值再指向拷贝后的地址
a1.set("temporary str1","temporary str2");

 template<typename T>
       void f_forward(T &&t) {
           Object a = std::forward<T>(t);
           std::cout << "forward this object, address: " << &a << std::endl;
       }
       int main() {
           Object obj{"abc"};
          f_forward(obj);
          f_forward(Object("def"));
          return 0;
        }
//输入为左值转发左值，输入为右值转发右值
```

# 4.semaphore 信号量

控制同时访问特定资源的线程数量，用于那些资源有明确访问数量限制的场景，常用于限流

```cpp
semaphore.acquire();//当前线程会尝试去同步队列获取一个令牌 ，失败进入阻塞队列
semaphore.release(); // 线程会尝试释放一个令牌，释放令牌成功之后，同时会唤醒同步队列中的一个线程
```

# 5. struct 字节对齐和字节补充

```c
typedef struct D
{
     short b; //4   对齐
     char a;  //4   对齐
     double c; //8
     char e;   //8   字节补充
}D;
sizeof(D) == 24 
```

# 6. future

```cpp
std::future<int> f2()
{
    return std::async(std::launch::async, [](std::function<std::future<int>(void)> previous)->int
    {
        std::cout<<"f2 start"<<std::endl;
        int ret = f1().get();
        std::cout<<"f2 middle, received return value "<<ret<<" from f1()"<<std::endl;
        std::this_thread::sleep_for(3s);
        std::cout<<"f2 end"<<std::endl;
        return ret + 111;
    },f1);
}
```
