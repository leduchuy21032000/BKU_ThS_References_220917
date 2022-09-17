
.data 
## String to be printed:
out_string: .asciiz "\nNhập vào số fibonaci muốn lấy:\n"
buffer: .space 4
result_notify: .asciiz "\n Kết quả là: "
.text 
main: 
li $v0, 4 
la $a0, out_string # Thông báo
syscall 

#Nhập số từ người dùng
li $v0, 5
syscall

####################


# s0: thanh ghi chứa số fibonachi cần tìm
move $s0, $v0

#Khởi tạo s1 bằng 0 để tính từng số fibo từ 1 cho đến s0
addi $s1 , $zero , 1

# hai thanh khi chứa giá trị cho index 1 và 2 (đều bằng 1)
addi $s5 , $zero , 1
addi $s6 , $zero , 2




loop:

addi $sp , $sp , -4
#Kiêm tra có phải ở index 1 hay 2 k (fibo = 1)
beq $s1, $s5, init
beq $s1, $s6, init  # nếu bằng thì  chuyển đến label init như một phép gán bình thường
# Hai thanh ghi s3,s4 lấy giá trị hai phần từ trong stack
lw $s3, 4($sp)  
lw $s4, 8($sp)
# thanh ghi s7 chứa giá trị tổng
add $s7, $s3, $s4

# Lưu giá trị vào trong stack
sw $s7, 0($sp)
continue:
# Kiểm tra index đã đến số cần tìm chưa. Nếu rồi thì gán giá trị vào t0 ở label result
beq $s1, $s0, result
# cộng 1 vào index s1
addi $s1, $s1, 1
j loop
j exit   	

init:
sw $s1, 0($sp)
j continue


result:
lw $t0 , 4($sp)

exit:

li $v0, 4 
la $a0, result_notify # Thông báo
syscall 

# In kết quả
li $v0, 1
move $a0, $t0
syscall

#########





li $v0, 10 # terminate program
syscall