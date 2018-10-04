using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;

namespace Assembler
{
    class Program
    {
        //C-Instruction
        private static Dictionary<String,String> dest = new Dictionary<string, string>();

        static void Main(string[] args)
        {
            String input = Console.ReadLine();
            String path = "D:\\Users\\igors\\Desktop\\" + input;

            Console.WriteLine("Abrindo arquivo " + path);

            //Checa se arquivo existe
            FileInfo info = new FileInfo(@path);
            if (info.Exists)
            {
                Parser(info.FullName);
            }

            Console.Read();

        }

        static void Parser(String filepath)
        {
            StreamReader streamReader = new StreamReader(@filepath);
            String line;
            while ((line = streamReader.ReadLine()) != null)
            {
                StringBuilder buffer = new StringBuilder();

                if (line.Contains("//") || line.Contains("\n") || line.Contains(" "))
                {
                    if (line.Contains("//")) { line = line.Remove(line.IndexOf('/')); }
                    line = line.Replace("\n", "").Replace("\t","").Replace("\r","");
                    line = line.Replace(" ", "");
                }
                if (line.Trim() != "") {
                    Console.WriteLine(line.Trim());
                }
            }
            streamReader.Close();

        }
        static void Tabelas()
        {
            dest.Add("","");
            dest.TryGetValue("", out string result);
            
        }
    }
}
